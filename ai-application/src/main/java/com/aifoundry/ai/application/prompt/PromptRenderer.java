package com.aifoundry.ai.application.prompt;

import com.aifoundry.platform.common.error.ValidationException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class PromptRenderer {
  private static final Pattern VARIABLE = Pattern.compile("\\{\\{([a-zA-Z0-9_.-]+)}}");

  public PromptModels.Rendered render(
      PromptModels.Template template, PromptModels.Context context) {
    Map<String, Object> variables = new HashMap<>(context.variables());
    variables.putIfAbsent("context", String.join("\n\n", context.retrievedContext()));
    variables.putIfAbsent(
        "conversation",
        context.conversation().stream()
            .map(message -> message.role() + ": " + message.content())
            .collect(Collectors.joining("\n")));

    Matcher matcher = VARIABLE.matcher(template.template());
    StringBuilder rendered = new StringBuilder();
    while (matcher.find()) {
      Object value = variables.get(matcher.group(1));
      if (value == null) {
        throw new ValidationException("Missing prompt variable: " + matcher.group(1));
      }
      matcher.appendReplacement(rendered, Matcher.quoteReplacement(String.valueOf(value)));
    }
    matcher.appendTail(rendered);
    return new PromptModels.Rendered(
        template.id(), template.version(), rendered.toString(), Map.copyOf(variables));
  }
}
