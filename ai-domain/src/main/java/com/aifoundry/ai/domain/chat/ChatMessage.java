package com.aifoundry.ai.domain.chat;

import java.util.*;

public record ChatMessage(ChatRole role, String content, Map<String, Object> metadata) {
  public ChatMessage {
    Objects.requireNonNull(role);
    if (content == null || content.isBlank())
      throw new IllegalArgumentException("content must not be blank");
    metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
  }
}
