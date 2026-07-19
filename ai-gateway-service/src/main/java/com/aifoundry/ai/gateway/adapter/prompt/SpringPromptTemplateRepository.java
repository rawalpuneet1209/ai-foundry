package com.aifoundry.ai.gateway.adapter.prompt;

import com.aifoundry.ai.application.prompt.PromptModels;
import com.aifoundry.ai.application.prompt.PromptTemplateRepository;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public final class SpringPromptTemplateRepository implements PromptTemplateRepository {
  private static final Pattern VARIABLE = Pattern.compile("\\{\\{([a-zA-Z0-9_.-]+)}}");
  private final Map<String, PromptModels.Template> templates;

  public SpringPromptTemplateRepository() {
    this(new PathMatchingResourcePatternResolver());
  }

  SpringPromptTemplateRepository(PathMatchingResourcePatternResolver resolver) {
    try {
      templates =
          Arrays.stream(resolver.getResources("classpath*:prompts/*.txt"))
              .map(this::load)
              .collect(
                  Collectors.toUnmodifiableMap(PromptModels.Template::id, Function.identity()));
    } catch (IOException exception) {
      throw new UncheckedIOException("Unable to discover prompt templates", exception);
    }
    if (templates.isEmpty()) {
      throw new IllegalStateException("No prompt templates found under classpath:/prompts");
    }
  }

  private PromptModels.Template load(Resource resource) {
    try {
      String filename = resource.getFilename();
      if (filename == null || !filename.endsWith(".txt")) {
        throw new IllegalStateException("Prompt resource requires a .txt filename");
      }
      String id = filename.substring(0, filename.length() - 4);
      String content = resource.getContentAsString(StandardCharsets.UTF_8).strip();
      Set<String> variables =
          VARIABLE
              .matcher(content)
              .results()
              .map(MatchResult::group)
              .map(this::variableName)
              .collect(Collectors.toUnmodifiableSet());
      return new PromptModels.Template(
          id, id, "1", content, variables, Map.of("resource", filename));
    } catch (IOException exception) {
      throw new UncheckedIOException("Unable to load prompt resource " + resource, exception);
    }
  }

  private String variableName(String token) {
    return token.substring(2, token.length() - 2);
  }

  @Override
  public Optional<PromptModels.Template> findById(String id) {
    return Optional.ofNullable(templates.get(id));
  }

  @Override
  public List<PromptModels.Template> findAll() {
    return templates.values().stream()
        .sorted(Comparator.comparing(PromptModels.Template::id))
        .toList();
  }
}
