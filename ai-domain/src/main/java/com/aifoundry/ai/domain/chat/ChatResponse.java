package com.aifoundry.ai.domain.chat;

import java.util.*;

public record ChatResponse(
    String responseId,
    String conversationId,
    String model,
    String content,
    TokenUsage tokenUsage,
    FinishReason finishReason,
    Map<String, Object> metadata) {
  public ChatResponse {
    metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    tokenUsage = tokenUsage == null ? TokenUsage.unknown() : tokenUsage;
    finishReason = finishReason == null ? FinishReason.UNKNOWN : finishReason;
  }
}
