package com.aifoundry.ai.domain.chat;

public enum FinishReason {
  STOP,
  LENGTH,
  TOOL_CALL,
  CONTENT_FILTER,
  ERROR,
  UNKNOWN
}
