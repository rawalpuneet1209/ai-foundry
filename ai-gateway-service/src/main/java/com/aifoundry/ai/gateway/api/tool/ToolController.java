package com.aifoundry.ai.gateway.api.tool;

import com.aifoundry.ai.application.tool.ToolServices.*;
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

  private final Registry registry;
  private final Executor executor;

  public ToolController(Registry r, Executor e) {
    registry = r;
    executor = e;
  }

  @GetMapping
  public List<Definition> tools() {
    return registry.definitions();
  }

  @PostMapping("/execute")
  public Result execute(@RequestBody ExecuteRequest r) {
    return executor.execute(
        new Request(UUID.randomUUID().toString(), r.toolName(), r.arguments(), r.context()),
        r.allowedTools() == null ? Set.of() : r.allowedTools());
  }
}
