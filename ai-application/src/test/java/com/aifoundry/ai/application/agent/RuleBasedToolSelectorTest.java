package com.aifoundry.ai.application.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RuleBasedToolSelectorTest {
  private final RuleBasedToolSelector selector = new RuleBasedToolSelector();

  @Test
  void selectsToolsFromNaturalLanguage() {
    assertEquals(
        "freeze-card",
        selector
            .select("Freeze my card", Map.of(), Set.of("freeze-card"))
            .orElseThrow()
            .toolName());
    assertEquals(
        "loan-eligibility-check",
        selector
            .select("Check my loan eligibility", Map.of(), Set.of("loan-eligibility-check"))
            .orElseThrow()
            .toolName());
    assertEquals(
        "account-summary",
        selector
            .select("Show my account balance", Map.of(), Set.of("account-summary"))
            .orElseThrow()
            .toolName());
  }

  @Test
  void neverSelectsToolOutsideAgentAllowList() {
    assertTrue(selector.select("Freeze my card", Map.of(), Set.of()).isEmpty());
  }
}
