package com.aifoundry.ai.domain.agent;

import java.util.*;

public final class AgentModels {
  private AgentModels() {}

  public record AgentId(String value) {
    public AgentId {
      if (value == null || value.isBlank()) throw new IllegalArgumentException("agent id required");
    }
  }

  public enum AgentType {
    SUPERVISOR,
    GENERAL_BANKING,
    FRAUD,
    LOAN,
    CREDIT_CARD,
    ACCOUNT,
    KNOWLEDGE
  }

  public enum ExecutionStatus {
    COMPLETED,
    FAILED,
    APPROVAL_REQUIRED,
    PARTIAL
  }

  public enum ActionStatus {
    PLANNED,
    EXECUTED,
    FAILED,
    WAITING_APPROVAL,
    REJECTED
  }

  public record Definition(
      AgentId id,
      String name,
      AgentType type,
      String description,
      Set<String> capabilities,
      Set<String> allowedTools,
      boolean approvalRequired) {
    public Definition {
      capabilities = Set.copyOf(capabilities);
      allowedTools = Set.copyOf(allowedTools);
    }
  }

  public record Request(
      String executionId,
      String conversationId,
      String userId,
      String message,
      Map<String, Object> context) {
    public Request {
      context = context == null ? Map.of() : Map.copyOf(context);
    }
  }

  public record Action(
      String actionId,
      String toolName,
      Map<String, Object> input,
      Map<String, Object> output,
      ActionStatus status) {}

  public record Response(
      String executionId,
      AgentId agentId,
      String content,
      ExecutionStatus status,
      List<Action> actions,
      Map<String, Object> metadata) {
    public Response {
      actions = List.copyOf(actions);
      metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
  }
}
