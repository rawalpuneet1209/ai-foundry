package com.aifoundry.ai.application.prompt;

import com.aifoundry.platform.common.error.ValidationException;
import java.util.*;

public final class PromptBuilder {
  public PromptModels.Rendered build(PromptModels.Template t, PromptModels.Context c) {
    Map<String, Object> vars = new HashMap<>(c.variables());
    vars.putIfAbsent("context", String.join("\n\n", c.retrievedContext()));
    vars.putIfAbsent(
        "conversation",
        c.conversation().stream()
            .map(m -> m.role() + ": " + m.content())
            .collect(java.util.stream.Collectors.joining("\n")));
    String out = t.template();
    for (String key : t.requiredVariables()) {
      Object value = vars.get(key);
      if (value == null) throw new ValidationException("Missing prompt variable: " + key);
      out = out.replace("{{" + key + "}}", String.valueOf(value));
    }
    return new PromptModels.Rendered(t.id(), t.version(), out, Map.copyOf(vars));
  }
}
