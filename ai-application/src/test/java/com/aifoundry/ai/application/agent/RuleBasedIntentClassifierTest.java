package com.aifoundry.ai.application.agent;

import static org.junit.jupiter.api.Assertions.*;

import com.aifoundry.ai.domain.agent.AgentModels.AgentType;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RuleBasedIntentClassifierTest {
  private final RuleBasedIntentClassifier c = new RuleBasedIntentClassifier();

  @Test
  void routesFraud() {
    assertEquals(AgentType.FRAUD, c.classify("Unauthorized payment", Map.of()));
  }

  @Test
  void fallsBack() {
    assertEquals(AgentType.GENERAL_BANKING, c.classify("hello", Map.of()));
  }
}
