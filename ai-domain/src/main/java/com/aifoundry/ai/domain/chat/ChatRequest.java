package com.aifoundry.ai.domain.chat;

import java.util.*;

public record ChatRequest(
    String conversationId,
    String model,
    List<ChatMessage> messages,
    ChatOptions options,
    Map<String, Object> metadata) {
  public ChatRequest {
    if (messages == null || messages.isEmpty())
      throw new IllegalArgumentException("messages required");
    messages = List.copyOf(messages);
    options = options == null ? ChatOptions.defaults() : options;
    metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
  }
}
