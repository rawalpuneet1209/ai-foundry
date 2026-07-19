package com.aifoundry.ai.application.prompt;

public final class PromptBuilder {
  private final PromptRenderer renderer;
  private final PromptValidator validator;

  public PromptBuilder(PromptRenderer renderer, PromptValidator validator) {
    this.renderer = renderer;
    this.validator = validator;
  }

  public PromptModels.Rendered build(PromptModels.Template template, PromptModels.Context context) {
    validator.validateTemplate(template);
    validator.validateContext(template, context);
    PromptModels.Rendered rendered = renderer.render(template, context);
    validator.validateRendered(rendered);
    return rendered;
  }
}
