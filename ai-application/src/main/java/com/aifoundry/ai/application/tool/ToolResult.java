package com.aifoundry.ai.application.tool;

import java.util.Map;

public record ToolResult(
    String requestId,
    String toolName,
    Status status,
    Map<String, Object> output,
    String errorMessage) {
  public enum Status {
    COMPLETED,
    APPROVAL_REQUIRED,
    REJECTED,
    FAILED
  }

  public ToolResult {
    output = output == null ? Map.of() : Map.copyOf(output);
  }

  public boolean success() {
    return status == Status.COMPLETED;
  }
}
