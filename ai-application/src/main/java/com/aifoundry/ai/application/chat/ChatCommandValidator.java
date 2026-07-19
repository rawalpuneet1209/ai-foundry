package com.aifoundry.ai.application.chat;

import com.aifoundry.platform.common.error.ValidationException;

public final class ChatCommandValidator {
  private final int maxLength;

  public ChatCommandValidator(int maxLength) {
    this.maxLength = maxLength;
  }

  public void validate(ChatCommand c) {
    if (c == null || c.message() == null || c.message().isBlank())
      throw new ValidationException("message must not be blank");
    if (c.message().length() > maxLength)
      throw new ValidationException("message exceeds maximum length");
    var o = c.options();
    if (o.temperature() != null && (o.temperature() < 0 || o.temperature() > 2))
      throw new ValidationException("temperature must be between 0 and 2");
    if (o.topP() != null && (o.topP() < 0 || o.topP() > 1))
      throw new ValidationException("topP must be between 0 and 1");
    if (o.maxTokens() != null && o.maxTokens() <= 0)
      throw new ValidationException("maxTokens must be positive");
  }
}
