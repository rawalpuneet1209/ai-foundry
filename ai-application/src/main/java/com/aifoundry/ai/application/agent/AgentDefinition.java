package com.aifoundry.ai.application.agent;

import com.aifoundry.ai.domain.agent.AgentModels.AgentId;
import com.aifoundry.ai.domain.agent.AgentModels.AgentType;
import java.util.Set;

public record AgentDefinition(
    AgentId id,
    String name,
    AgentType type,
    String description,
    Set<String> capabilities,
    Set<String> allowedTools,
    boolean humanApprovalEnabled) {
  public AgentDefinition {
    capabilities = capabilities == null ? Set.of() : Set.copyOf(capabilities);
    allowedTools = allowedTools == null ? Set.of() : Set.copyOf(allowedTools);
  }
}
