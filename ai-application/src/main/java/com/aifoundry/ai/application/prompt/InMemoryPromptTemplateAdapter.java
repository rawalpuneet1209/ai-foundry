package com.aifoundry.ai.application.prompt;

import java.util.*;

public final class InMemoryPromptTemplateAdapter {
  private final Map<String, PromptModels.Template> templates;

  public InMemoryPromptTemplateAdapter() {
    Map<String, PromptModels.Template> m = new LinkedHashMap<>();
    add(
        m,
        "chat-default",
        "Answer clearly and acknowledge uncertainty. {{question}}",
        Set.of("question"));
    add(
        m,
        "rag-banking",
        "Use supplied context and cite chunks. {{context}} {{conversation}} {{question}}",
        Set.of("context", "conversation", "question"));
    for (String id :
        List.of(
            "agent-supervisor",
            "agent-general-banking",
            "agent-fraud",
            "agent-loan",
            "agent-credit-card",
            "agent-account",
            "agent-knowledge"))
      add(m, id, "Follow role safety and approval rules. {{question}}", Set.of("question"));
    templates = Map.copyOf(m);
  }

  private void add(Map<String, PromptModels.Template> m, String id, String body, Set<String> vars) {
    m.put(id, new PromptModels.Template(id, id, "1", body, vars, Map.of()));
  }

  public Optional<PromptModels.Template> findById(String id) {
    return Optional.ofNullable(templates.get(id));
  }

  public PromptModels.Template getRequired(String id) {
    return findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Prompt template not found: " + id));
  }

  public List<PromptModels.Template> findAll() {
    return List.copyOf(templates.values());
  }
}
