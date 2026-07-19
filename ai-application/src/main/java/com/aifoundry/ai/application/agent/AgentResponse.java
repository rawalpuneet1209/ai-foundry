package com.aifoundry.ai.application.agent;

import com.aifoundry.ai.domain.agent.AgentModels.Action;
import com.aifoundry.ai.domain.agent.AgentModels.AgentId;
import com.aifoundry.ai.domain.agent.AgentModels.ExecutionStatus;
import java.util.List;
import java.util.Map;

public record AgentResponse(
    String executionId,
    AgentId agentId,
    String content,
    ExecutionStatus status,
    List<Action> actions,
    Map<String, Object> metadata) {
  public AgentResponse {
    actions = actions == null ? List.of() : List.copyOf(actions);
    metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
  }
}
