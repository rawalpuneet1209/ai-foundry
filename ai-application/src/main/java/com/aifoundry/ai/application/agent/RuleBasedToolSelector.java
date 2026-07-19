package com.aifoundry.ai.application.agent;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class RuleBasedToolSelector implements ToolSelector {
  @Override
  public Optional<Selection> select(
      String message, Map<String, Object> context, Set<String> allowedTools) {
    String normalized = message == null ? "" : message.toLowerCase(Locale.ROOT);
    String toolName = selectTool(normalized);
    if (toolName == null || !allowedTools.contains(toolName)) {
      return Optional.empty();
    }
    return Optional.of(new Selection(toolName, arguments(toolName, context)));
  }

  private String selectTool(String message) {
    if (contains(message, "freeze", "block card", "stolen card")) {
      return "freeze-card";
    }
    if (contains(message, "replace card", "replacement card", "new card")) {
      return "card-replacement-request";
    }
    if (contains(message, "loan eligibility", "eligible for a loan", "check eligibility")) {
      return "loan-eligibility-check";
    }
    if (contains(message, "account balance", "account summary", "show my balance", "my balance")) {
      return "account-summary";
    }
    if (contains(message, "transaction", "recent debit", "recent payment")) {
      return "transaction-lookup";
    }
    if (contains(message, "card details", "card limit", "available limit")) {
      return "card-details";
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> arguments(String toolName, Map<String, Object> context) {
    Map<String, Object> arguments = new HashMap<>();
    Object supplied = context.get("arguments");
    if (supplied instanceof Map<?, ?> map) {
      arguments.putAll((Map<String, Object>) map);
    }
    copy(
        context,
        arguments,
        "accountId",
        "cardId",
        "monthlyIncome",
        "monthlyDebt",
        "requestedAmount");
    if (Set.of("account-summary", "transaction-lookup").contains(toolName)) {
      arguments.putIfAbsent("accountId", "current-account");
    }
    if (Set.of("freeze-card", "card-replacement-request", "card-details").contains(toolName)) {
      arguments.putIfAbsent("cardId", "current-card");
    }
    return arguments;
  }

  private void copy(Map<String, Object> source, Map<String, Object> target, String... keys) {
    for (String key : keys) {
      if (source.containsKey(key)) {
        target.put(key, source.get(key));
      }
    }
  }

  private boolean contains(String message, String... values) {
    for (String value : values) {
      if (message.contains(value)) {
        return true;
      }
    }
    return false;
  }
}
