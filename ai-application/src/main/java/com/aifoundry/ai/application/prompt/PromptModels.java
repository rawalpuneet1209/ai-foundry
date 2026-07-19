package com.aifoundry.ai.application.prompt;

import com.aifoundry.ai.domain.chat.ChatMessage;
import java.util.*;

public final class PromptModels {
  private PromptModels() {}

  public record Template(
      String id,
      String name,
      String version,
      String template,
      Set<String> requiredVariables,
      Map<String, Object> metadata) {
    public Template {
      requiredVariables = Set.copyOf(requiredVariables);
      metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
  }

  public record Context(
      Map<String, Object> variables,
      List<ChatMessage> conversation,
      List<String> retrievedContext,
      Map<String, Object> metadata) {
    public Context {
      variables = variables == null ? Map.of() : Map.copyOf(variables);
      conversation = conversation == null ? List.of() : List.copyOf(conversation);
      retrievedContext = retrievedContext == null ? List.of() : List.copyOf(retrievedContext);
      metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
  }

  public record Rendered(
      String templateId, String templateVersion, String content, Map<String, Object> variables) {}
}
