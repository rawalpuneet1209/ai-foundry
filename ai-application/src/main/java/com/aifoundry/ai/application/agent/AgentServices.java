package com.aifoundry.ai.application.agent;

import com.aifoundry.ai.domain.agent.AgentModels.*;
import java.util.*;
import java.util.concurrent.*;

public final class AgentServices {
  private AgentServices() {}

  public interface Agent {
    Definition definition();

    Response execute(Request request);
  }

  public interface IntentClassifier {
    AgentType classify(String message, Map<String, Object> context);
  }

  public static final class RuleBasedIntentClassifier implements IntentClassifier {
    public AgentType classify(String m, Map<String, Object> c) {
      String s = m == null ? "" : m.toLowerCase(Locale.ROOT);
      if (matches(s, "fraud", "stolen", "suspicious", "unauthorized")) return AgentType.FRAUD;
      if (matches(s, "loan", "mortgage", "emi", "interest", "eligibility")) return AgentType.LOAN;
      if (matches(s, "card", "limit", "statement")) return AgentType.CREDIT_CARD;
      if (matches(s, "balance", "account", "transaction", "debit")) return AgentType.ACCOUNT;
      return AgentType.GENERAL_BANKING;
    }

    private boolean matches(String s, String... ks) {
      return Arrays.stream(ks).anyMatch(s::contains);
    }
  }

  public static final class Registry {
    private final ConcurrentMap<AgentId, Agent> agents = new ConcurrentHashMap<>();

    public void register(Agent a) {
      if (agents.putIfAbsent(a.definition().id(), a) != null)
        throw new IllegalArgumentException("Duplicate agent: " + a.definition().id().value());
    }

    public Optional<Agent> find(AgentId id) {
      return Optional.ofNullable(agents.get(id));
    }

    public List<Definition> definitions() {
      return agents.values().stream()
          .map(Agent::definition)
          .sorted(Comparator.comparing(d -> d.id().value()))
          .toList();
    }

    public Optional<Agent> byType(AgentType t) {
      return agents.values().stream().filter(a -> a.definition().type() == t).findFirst();
    }
  }
}
