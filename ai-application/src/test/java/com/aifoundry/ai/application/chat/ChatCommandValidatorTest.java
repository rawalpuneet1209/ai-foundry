package com.aifoundry.ai.application.chat;

import static org.junit.jupiter.api.Assertions.*;

import com.aifoundry.ai.domain.chat.ChatOptions;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ChatCommandValidatorTest {
  @Test
  void rejectsBlankMessage() {
    assertThrows(
        RuntimeException.class,
        () ->
            new ChatCommandValidator(10)
                .validate(
                    new ChatCommand(
                        null, null, null, " ", ChatOptions.defaults(), false, Map.of())));
  }

  @Test
  void acceptsValidCommand() {
    assertDoesNotThrow(
        () ->
            new ChatCommandValidator(10)
                .validate(
                    new ChatCommand(
                        null, null, null, "hello", ChatOptions.defaults(), false, Map.of())));
  }
}
