package com.aifoundry.ai.gateway.api.agent;

import com.aifoundry.ai.application.agent.*;
import com.aifoundry.ai.domain.agent.AgentModels.*;
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

  private final AgentServices.Registry registry;
  private final BankingAgents.Supervisor supervisor;

  public AgentController(AgentServices.Registry r, BankingAgents.Supervisor s) {
    registry = r;
    supervisor = s;
  }

  @GetMapping
  public List<Definition> agents() {
    return registry.definitions();
  }

  @PostMapping("/execute")
  public Response execute(@Valid @RequestBody ExecuteRequest r) {
    return supervisor.execute(
        new Request(
            UUID.randomUUID().toString(),
            r.conversationId(),
            r.userId(),
            r.message(),
            r.context()));
  }
}
