package com.aifoundry.ai.application.prompt;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;

class PromptBuilderTest {
  @Test
  void rendersRequiredVariables() {
    var t = new PromptModels.Template("x", "x", "1", "Hello {{name}}", Set.of("name"), Map.of());
    var r =
        new PromptBuilder()
            .build(
                t, new PromptModels.Context(Map.of("name", "Ada"), List.of(), List.of(), Map.of()));
    assertEquals("Hello Ada", r.content());
  }

  @Test
  void rejectsMissingVariables() {
    var t = new PromptModels.Template("x", "x", "1", "{{name}}", Set.of("name"), Map.of());
    assertThrows(
        RuntimeException.class,
        () ->
            new PromptBuilder()
                .build(t, new PromptModels.Context(Map.of(), List.of(), List.of(), Map.of())));
  }
}
