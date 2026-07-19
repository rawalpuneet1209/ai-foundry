package com.aifoundry.ai.application.chat;

import com.aifoundry.ai.domain.chat.ChatOptions;
import java.util.*;

public record ChatCommand(
    String conversationId,
    String userId,
    String model,
    String message,
    ChatOptions options,
    boolean useRag,
    Map<String, Object> metadata) {
  public ChatCommand {
    options = options == null ? ChatOptions.defaults() : options;
    metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
  }
}
