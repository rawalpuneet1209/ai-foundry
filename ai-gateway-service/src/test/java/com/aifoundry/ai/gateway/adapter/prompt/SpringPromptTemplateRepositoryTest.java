package com.aifoundry.ai.gateway.adapter.prompt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SpringPromptTemplateRepositoryTest {
  @Test
  void loadsTemplatesAndVariablesFromResources() {
    SpringPromptTemplateRepository repository = new SpringPromptTemplateRepository();

    var template = repository.getRequired("rag-banking");

    assertEquals("rag-banking", template.id());
    assertTrue(template.requiredVariables().contains("context"));
    assertEquals(1, template.requiredVariables().size());
    assertTrue(repository.findAll().size() >= 9);
  }
}
