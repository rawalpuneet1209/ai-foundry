package com.aifoundry.ai.application.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.aifoundry.ai.domain.agent.AgentModels.AgentId;
import com.aifoundry.ai.domain.agent.AgentModels.AgentType;
import com.aifoundry.ai.domain.agent.AgentModels.ExecutionStatus;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AgentSupervisorTest {
  @Test
  void routesClassifiedIntentToMatchingAgent() {
    DefaultAgentRegistry registry = new DefaultAgentRegistry();
    registry.register(agent(AgentType.FRAUD));
    registry.register(agent(AgentType.GENERAL_BANKING));
    AgentSupervisor supervisor = new AgentSupervisor(new RuleBasedIntentClassifier(), registry);

    AgentResponse response =
        supervisor.execute(
            new AgentRequest(null, "conversation", "user", "unauthorized payment", Map.of()));

    assertEquals("fraud", response.agentId().value());
    assertEquals("FRAUD", response.metadata().get("classifiedIntent"));
  }

  private Agent agent(AgentType type) {
    return new Agent() {
      private final AgentDefinition definition =
          new AgentDefinition(
              new AgentId(type.name().toLowerCase()),
              type.name(),
              type,
              type.name(),
              Set.of(),
              Set.of(),
              false);

      @Override
      public AgentDefinition definition() {
        return definition;
      }

      @Override
      public AgentResponse execute(AgentRequest request) {
        return new AgentResponse(
            request.executionId(),
            definition.id(),
            "done",
            ExecutionStatus.COMPLETED,
            List.of(),
            Map.of());
      }
    };
  }
}
