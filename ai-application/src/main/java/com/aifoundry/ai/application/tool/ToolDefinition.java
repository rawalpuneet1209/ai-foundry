package com.aifoundry.ai.application.tool;

import java.util.Map;

public record ToolDefinition(
    String name, String description, Map<String, Object> inputSchema, boolean approvalRequired) {
  public ToolDefinition {
    inputSchema = inputSchema == null ? Map.of() : Map.copyOf(inputSchema);
  }
}
