package com.aifoundry.ai.application.prompt;

import java.util.List;
import java.util.Optional;

public interface PromptTemplateRepository {
  Optional<PromptModels.Template> findById(String id);

  default PromptModels.Template getRequired(String id) {
    return findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Prompt template not found: " + id));
  }

  List<PromptModels.Template> findAll();
}
