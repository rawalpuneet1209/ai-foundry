package com.aifoundry.ai.application.agent;

import com.aifoundry.ai.domain.agent.AgentModels.AgentId;
import com.aifoundry.ai.domain.agent.AgentModels.AgentType;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class DefaultAgentRegistry implements AgentRegistry {
  private final ConcurrentMap<AgentId, Agent> agents = new ConcurrentHashMap<>();

  @Override
  public void register(Agent agent) {
    if (agents.putIfAbsent(agent.definition().id(), agent) != null) {
      throw new IllegalArgumentException("Duplicate agent: " + agent.definition().id().value());
    }
  }

  @Override
  public Optional<Agent> find(AgentId id) {
    return Optional.ofNullable(agents.get(id));
  }

  @Override
  public Optional<Agent> byType(AgentType type) {
    return agents.values().stream().filter(agent -> agent.definition().type() == type).findFirst();
  }

  @Override
  public List<AgentDefinition> definitions() {
    return agents.values().stream()
        .map(Agent::definition)
        .sorted(Comparator.comparing(definition -> definition.id().value()))
        .toList();
  }
}
