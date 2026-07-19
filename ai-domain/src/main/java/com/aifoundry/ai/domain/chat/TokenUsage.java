package com.aifoundry.ai.domain.chat;

public record TokenUsage(long promptTokens, long completionTokens, long totalTokens) {
  public static TokenUsage unknown() {
    return new TokenUsage(0, 0, 0);
  }
}
