package com.aifoundry.ai.application.agent;

import com.aifoundry.ai.domain.agent.AgentModels.AgentType;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

public final class RuleBasedIntentClassifier implements IntentClassifier {
  @Override
  public AgentType classify(String message, Map<String, Object> context) {
    String normalized = message == null ? "" : message.toLowerCase(Locale.ROOT);
    if (matches(normalized, "fraud", "stolen", "suspicious", "unauthorized")) {
      return AgentType.FRAUD;
    }
    if (matches(normalized, "loan", "mortgage", "emi", "interest", "eligibility")) {
      return AgentType.LOAN;
    }
    if (matches(normalized, "card", "limit", "statement")) {
      return AgentType.CREDIT_CARD;
    }
    if (matches(normalized, "balance", "account", "transaction", "debit")) {
      return AgentType.ACCOUNT;
    }
    if (matches(normalized, "policy", "knowledge", "documentation")) {
      return AgentType.KNOWLEDGE;
    }
    return AgentType.GENERAL_BANKING;
  }

  private boolean matches(String value, String... keywords) {
    return Arrays.stream(keywords).anyMatch(value::contains);
  }
}
