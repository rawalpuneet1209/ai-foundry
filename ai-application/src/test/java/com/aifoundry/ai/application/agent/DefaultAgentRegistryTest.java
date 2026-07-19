package com.aifoundry.ai.application.agent;

import static org.junit.jupiter.api.Assertions.*;

import com.aifoundry.ai.domain.agent.AgentModels.AgentId;
import com.aifoundry.ai.domain.agent.AgentModels.AgentType;
import java.util.*;
import org.junit.jupiter.api.Test;

class DefaultAgentRegistryTest {
  @Test
  void rejectsDuplicates() {
    var r = new DefaultAgentRegistry();
    Agent a =
        new Agent() {
          public AgentDefinition definition() {
            return new AgentDefinition(
                new AgentId("x"), "x", AgentType.ACCOUNT, "x", Set.of(), Set.of(), false);
          }

          public AgentResponse execute(AgentRequest q) {
            return null;
          }
        };
    r.register(a);
    assertThrows(IllegalArgumentException.class, () -> r.register(a));
  }
}
