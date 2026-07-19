package com.aifoundry.ai.application.prompt;

import com.aifoundry.platform.common.error.ValidationException;
import java.util.regex.Pattern;

public final class PromptValidator {
  private static final Pattern CONTROL = Pattern.compile("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]");
  private final int max;

  public PromptValidator(int max) {
    this.max = max;
  }

  public void validateTemplate(PromptModels.Template t) {
    if (t == null
        || t.id() == null
        || t.id().isBlank()
        || t.template() == null
        || t.template().isBlank()) throw new ValidationException("Prompt template is invalid");
    for (String v : t.requiredVariables())
      if (!t.template().contains("{{" + v + "}}"))
        throw new ValidationException("Template does not contain required variable: " + v);
  }

  public void validateContext(PromptModels.Template t, PromptModels.Context c) {
    for (String v : t.requiredVariables())
      if (!v.equals("context") && !v.equals("conversation") && !c.variables().containsKey(v))
        throw new ValidationException("Missing prompt variable: " + v);
    if (t.requiredVariables().contains("context") && c.retrievedContext().isEmpty())
      throw new ValidationException("RAG context is required");
  }

  public void validateRendered(PromptModels.Rendered r) {
    if (r == null || r.content() == null || r.content().isBlank())
      throw new ValidationException("Rendered prompt is blank");
    if (r.content().length() > max)
      throw new ValidationException("Rendered prompt exceeds maximum length");
    if (CONTROL.matcher(r.content()).find())
      throw new ValidationException("Rendered prompt contains control characters");
  }
}
