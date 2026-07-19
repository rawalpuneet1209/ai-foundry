package com.aifoundry.ai.application.agent;

import java.util.Map;

public record AgentRequest(
    String executionId,
    String conversationId,
    String userId,
    String message,
    Map<String, Object> context) {
  public AgentRequest {
    context = context == null ? Map.of() : Map.copyOf(context);
  }
}
