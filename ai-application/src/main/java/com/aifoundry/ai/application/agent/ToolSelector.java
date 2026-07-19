package com.aifoundry.ai.application.agent;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ToolSelector {
  record Selection(String toolName, Map<String, Object> arguments) {
    public Selection {
      arguments = arguments == null ? Map.of() : Map.copyOf(arguments);
    }
  }

  Optional<Selection> select(String message, Map<String, Object> context, Set<String> allowedTools);
}
