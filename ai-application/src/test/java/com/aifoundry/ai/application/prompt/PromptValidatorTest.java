package com.aifoundry.ai.application.prompt;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;

class PromptValidatorTest {
  @Test
  void rejectsOversizedPrompt() {
    assertThrows(
        RuntimeException.class,
        () ->
            new PromptValidator(3)
                .validateRendered(new PromptModels.Rendered("x", "1", "long", Map.of())));
  }

  @Test
  void validatesTemplateVariables() {
    var t = new PromptModels.Template("x", "x", "1", "{{question}}", Set.of("question"), Map.of());
    assertDoesNotThrow(() -> new PromptValidator(100).validateTemplate(t));
  }
}
