package com.aifoundry.ai.application.agent;

import com.aifoundry.ai.domain.agent.AgentModels.AgentId;
import com.aifoundry.ai.domain.agent.AgentModels.AgentType;
import java.util.List;
import java.util.Optional;

public interface AgentRegistry {
  void register(Agent agent);

  Optional<Agent> find(AgentId id);

  Optional<Agent> byType(AgentType type);

  List<AgentDefinition> definitions();
}
