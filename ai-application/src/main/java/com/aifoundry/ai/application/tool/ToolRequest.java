package com.aifoundry.ai.application.tool;

import java.util.Map;

public record ToolRequest(
    String requestId, String toolName, Map<String, Object> arguments, Map<String, Object> context) {
  public ToolRequest {
    arguments = arguments == null ? Map.of() : Map.copyOf(arguments);
    context = context == null ? Map.of() : Map.copyOf(context);
  }
}
