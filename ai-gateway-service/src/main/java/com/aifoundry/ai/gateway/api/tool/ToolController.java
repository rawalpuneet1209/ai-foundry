package com.aifoundry.ai.gateway.api.tool;

import com.aifoundry.ai.application.tool.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tools")
public class ToolController {
  public record ExecuteRequest(
      String toolName,
      Map<String, Object> arguments,
      Set<String> allowedTools,
      Map<String, Object> context) {}

  private final ToolRegistry registry;
  private final ToolExecutionService executor;

  public ToolController(ToolRegistry r, ToolExecutionService e) {
    registry = r;
    executor = e;
  }

  @GetMapping
  public List<ToolDefinition> tools() {
    return registry.definitions();
  }

  @PostMapping("/execute")
  public ToolResult execute(@RequestBody ExecuteRequest r) {
    return executor.execute(
        new ToolRequest(UUID.randomUUID().toString(), r.toolName(), r.arguments(), r.context()),
        r.allowedTools() == null ? Set.of() : r.allowedTools());
  }
}
