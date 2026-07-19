package com.aifoundry.ai.domain.chat;

import java.util.*;

public record ChatOptions(
    Double temperature,
    Double topP,
    Integer maxTokens,
    List<String> stopSequences,
    boolean stream) {
  public ChatOptions {
    stopSequences = stopSequences == null ? List.of() : List.copyOf(stopSequences);
  }

  public static ChatOptions defaults() {
    return new ChatOptions(.2, .9, 1024, List.of(), false);
  }
}
