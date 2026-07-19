package com.aifoundry.ai.application.agent;

import static org.junit.jupiter.api.Assertions.*;

import com.aifoundry.ai.domain.agent.AgentModels.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class DefaultAgentRegistryTest {
  @Test
  void rejectsDuplicates() {
    var r = new AgentServices.Registry();
    AgentServices.Agent a =
        new AgentServices.Agent() {
          public Definition definition() {
            return new Definition(
                new AgentId("x"), "x", AgentType.ACCOUNT, "x", Set.of(), Set.of(), false);
          }

          public Response execute(Request q) {
            return null;
          }
        };
    r.register(a);
    assertThrows(IllegalArgumentException.class, () -> r.register(a));
  }
}
