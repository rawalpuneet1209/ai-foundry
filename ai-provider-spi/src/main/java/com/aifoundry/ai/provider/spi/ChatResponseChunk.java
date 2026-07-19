package com.aifoundry.ai.provider.spi;

import com.aifoundry.ai.domain.chat.FinishReason;
import java.util.*;

public record ChatResponseChunk(
    String responseId,
    String conversationId,
    String model,
    String delta,
    boolean completed,
    FinishReason finishReason,
    Map<String, Object> metadata) {
  public ChatResponseChunk {
    metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
  }
}
