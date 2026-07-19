package com.aifoundry.ai.application.agent;

import com.aifoundry.ai.domain.agent.AgentModels.*;
import com.aifoundry.ai.domain.chat.*;
import com.aifoundry.ai.provider.spi.ChatProvider;
import java.util.*;

public final class BankingAgents {
  private BankingAgents() {}

  public abstract static class BankingAgent implements AgentServices.Agent {
    private final Definition definition;
    private final ChatProvider chat;

    protected BankingAgent(
        ChatProvider c,
        String id,
        String name,
        AgentType type,
        Set<String> tools,
        boolean approval) {
      chat = c;
      definition =
          new Definition(
              new AgentId(id),
              name,
              type,
              name + " specialist",
              Set.of("CHAT", "SAFE_GUIDANCE"),
              tools,
              approval);
    }

    public Definition definition() {
      return definition;
    }

    protected abstract String safety();

    public Response execute(Request r) {
      if (r.message() == null || r.message().isBlank())
        return new Response(
            r.executionId(),
            definition.id(),
            "A message is required.",
            ExecutionStatus.FAILED,
            List.of(),
            Map.of());
      String prompt = safety() + "\nUser request: " + r.message();
      try {
        var result =
            chat.chat(
                new ChatRequest(
                    r.conversationId(),
                    null,
                    List.of(
                        new ChatMessage(ChatRole.SYSTEM, prompt, Map.of()),
                        new ChatMessage(ChatRole.USER, r.message(), Map.of())),
                    ChatOptions.defaults(),
                    Map.of("agentId", definition.id().value())));
        return new Response(
            r.executionId(),
            definition.id(),
            result.content(),
            ExecutionStatus.COMPLETED,
            List.of(),
            Map.of("agentType", definition.type().name()));
      } catch (Exception e) {
        return new Response(
            r.executionId(),
            definition.id(),
            "The assistant could not complete the request safely. Please try again.",
            ExecutionStatus.FAILED,
            List.of(),
            Map.of("agentType", definition.type().name()));
      }
    }
  }

  public static final class GeneralBankingAgent extends BankingAgent {
    public GeneralBankingAgent(ChatProvider c) {
      super(
          c,
          "general-banking-agent",
          "General Banking",
          AgentType.GENERAL_BANKING,
          Set.of(),
          false);
    }

    protected String safety() {
      return "Give general banking guidance. Do not invent facts or claim actions occurred.";
    }
  }

  public static final class FraudAgent extends BankingAgent {
    public FraudAgent(ChatProvider c) {
      super(
          c,
          "fraud-agent",
          "Fraud",
          AgentType.FRAUD,
          Set.of("transaction-lookup", "freeze-card"),
          true);
    }

    protected String safety() {
      return "Prioritize safety. A card freeze requires approval. Never claim an action succeeded"
          + " without a confirmed tool result.";
    }
  }

  public static final class LoanAgent extends BankingAgent {
    public LoanAgent(ChatProvider c) {
      super(c, "loan-agent", "Loan", AgentType.LOAN, Set.of("loan-eligibility-check"), false);
    }

    protected String safety() {
      return "Label calculations illustrative and never present a final underwriting decision.";
    }
  }

  public static final class CreditCardAgent extends BankingAgent {
    public CreditCardAgent(ChatProvider c) {
      super(
          c,
          "credit-card-agent",
          "Credit Card",
          AgentType.CREDIT_CARD,
          Set.of("card-details", "card-replacement-request"),
          true);
    }

    protected String safety() {
      return "Mask card data. Replacement requires approval and must not be claimed successful"
          + " without a tool result.";
    }
  }

  public static final class AccountAgent extends BankingAgent {
    public AccountAgent(ChatProvider c) {
      super(
          c,
          "account-agent",
          "Account",
          AgentType.ACCOUNT,
          Set.of("account-summary", "transaction-lookup"),
          false);
    }

    protected String safety() {
      return "Mask account numbers. Do not initiate or claim transactions.";
    }
  }

  public static final class KnowledgeAgent extends BankingAgent {
    public KnowledgeAgent(ChatProvider c) {
      super(c, "knowledge-agent", "Knowledge", AgentType.KNOWLEDGE, Set.of(), false);
    }

    protected String safety() {
      return "Use supplied knowledge only, cite evidence, and say when context is insufficient.";
    }
  }

  public static final class Supervisor {
    private final AgentServices.IntentClassifier classifier;
    private final AgentServices.Registry registry;

    public Supervisor(AgentServices.IntentClassifier c, AgentServices.Registry r) {
      classifier = c;
      registry = r;
    }

    public Response execute(Request request) {
      String execution =
          request.executionId() == null || request.executionId().isBlank()
              ? UUID.randomUUID().toString()
              : request.executionId();
      Request normalized =
          new Request(
              execution,
              request.conversationId(),
              request.userId(),
              request.message(),
              request.context());
      AgentType type = classifier.classify(request.message(), request.context());
      var agent = registry.byType(type).or(() -> registry.byType(AgentType.GENERAL_BANKING));
      if (agent.isEmpty())
        return new Response(
            execution,
            new AgentId("supervisor"),
            "No suitable agent is available.",
            ExecutionStatus.FAILED,
            List.of(),
            Map.of("classifiedIntent", type.name()));
      Response response = agent.get().execute(normalized);
      Map<String, Object> metadata = new HashMap<>(response.metadata());
      metadata.put("classifiedIntent", type.name());
      metadata.put("selectedAgent", agent.get().definition().id().value());
      return new Response(
          response.executionId(),
          response.agentId(),
          response.content(),
          response.status(),
          response.actions(),
          metadata);
    }
  }
}
