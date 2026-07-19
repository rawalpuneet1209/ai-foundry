package com.aifoundry.ai.gateway.api.agent;

import com.aifoundry.ai.application.agent.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agents")
public class AgentController {
  public record ExecuteRequest(
      String conversationId,
      String userId,
      @NotBlank String message,
      Map<String, Object> context) {}

  private final AgentRegistry registry;
  private final AgentSupervisor supervisor;

  public AgentController(AgentRegistry r, AgentSupervisor s) {
    registry = r;
    supervisor = s;
  }

  @GetMapping
  public List<AgentDefinition> agents() {
    return registry.definitions();
  }

  @PostMapping("/execute")
  public AgentResponse execute(@Valid @RequestBody ExecuteRequest r) {
    return supervisor.execute(
        new AgentRequest(
            UUID.randomUUID().toString(),
            r.conversationId(),
            r.userId(),
            r.message(),
            r.context()));
  }
}
