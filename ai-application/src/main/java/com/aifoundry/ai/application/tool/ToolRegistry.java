package com.aifoundry.ai.application.tool;

import java.util.List;
import java.util.Optional;

public interface ToolRegistry {
  void register(Tool tool);

  Optional<Tool> find(String name);

  List<ToolDefinition> definitions();
}
