package com.aifoundry.ai.application.tool;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class DefaultToolRegistry implements ToolRegistry {
  private final ConcurrentMap<String, Tool> tools = new ConcurrentHashMap<>();

  @Override
  public void register(Tool tool) {
    if (tools.putIfAbsent(tool.definition().name(), tool) != null) {
      throw new IllegalArgumentException("Duplicate tool: " + tool.definition().name());
    }
  }

  @Override
  public Optional<Tool> find(String name) {
    return Optional.ofNullable(tools.get(name));
  }

  @Override
  public List<ToolDefinition> definitions() {
    return tools.values().stream()
        .map(Tool::definition)
        .sorted(Comparator.comparing(ToolDefinition::name))
        .toList();
  }
}
