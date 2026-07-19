package com.aifoundry.ai.application.tool;

import com.aifoundry.platform.common.error.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

public final class ToolServices {
  private ToolServices() {}

  public record Definition(
      String name, String description, Map<String, Object> inputSchema, boolean approvalRequired) {}

  public record Request(
      String requestId,
      String toolName,
      Map<String, Object> arguments,
      Map<String, Object> context) {
    public Request {
      arguments = arguments == null ? Map.of() : Map.copyOf(arguments);
      context = context == null ? Map.of() : Map.copyOf(context);
    }
  }

  public record Result(
      String requestId,
      String toolName,
      boolean success,
      Map<String, Object> output,
      String errorMessage) {
    public Result {
      output = output == null ? Map.of() : Map.copyOf(output);
    }
  }

  public interface Tool {
    Definition definition();

    Result execute(Request request);
  }

  public static final class Registry {
    private final ConcurrentMap<String, Tool> tools = new ConcurrentHashMap<>();

    public void register(Tool t) {
      if (tools.putIfAbsent(t.definition().name(), t) != null)
        throw new IllegalArgumentException("Duplicate tool: " + t.definition().name());
    }

    public Optional<Tool> find(String n) {
      return Optional.ofNullable(tools.get(n));
    }

    public List<Definition> definitions() {
      return tools.values().stream()
          .map(Tool::definition)
          .sorted(Comparator.comparing(Definition::name))
          .toList();
    }
  }

  public enum ApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED,
    EXPIRED
  }

  public record ApprovalRequest(
      String approvalId,
      String userId,
      String action,
      String description,
      Map<String, Object> payload,
      Instant expiresAt) {}

  public record ApprovalDecision(
      String approvalId,
      ApprovalStatus status,
      String decidedBy,
      Instant decidedAt,
      String comment) {}

  public static final class ApprovalService {
    private final ConcurrentMap<String, ApprovalRequest> requests = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ApprovalDecision> decisions = new ConcurrentHashMap<>();

    public ApprovalDecision request(ApprovalRequest r) {
      requests.put(r.approvalId(), r);
      return decisions.computeIfAbsent(
          r.approvalId(), id -> new ApprovalDecision(id, ApprovalStatus.PENDING, null, null, null));
    }

    public Optional<ApprovalDecision> find(String id) {
      ApprovalRequest r = requests.get(id);
      ApprovalDecision d = decisions.get(id);
      if (r != null
          && d != null
          && d.status() == ApprovalStatus.PENDING
          && r.expiresAt().isBefore(Instant.now())) {
        d =
            new ApprovalDecision(
                id, ApprovalStatus.EXPIRED, null, Instant.now(), "Approval expired");
        decisions.put(id, d);
      }
      return Optional.ofNullable(d);
    }

    public ApprovalDecision decide(String id, boolean approved, String by, String comment) {
      ApprovalRequest r = requests.get(id);
      if (r == null) throw new ValidationException("Approval not found");
      ApprovalDecision current = find(id).orElseThrow();
      if (current.status() != ApprovalStatus.PENDING)
        throw new ValidationException("Approval is no longer pending");
      ApprovalDecision d =
          new ApprovalDecision(
              id,
              approved ? ApprovalStatus.APPROVED : ApprovalStatus.REJECTED,
              by,
              Instant.now(),
              comment);
      decisions.put(id, d);
      return d;
    }
  }

  public static final class Executor {
    private final Registry registry;
    private final ApprovalService approvals;

    public Executor(Registry r, ApprovalService a) {
      registry = r;
      approvals = a;
    }

    public Result execute(Request r, Set<String> allowed) {
      if (!allowed.contains(r.toolName()))
        return new Result(
            r.requestId(), r.toolName(), false, Map.of(), "Tool is not allowed for this agent");
      Tool t = registry.find(r.toolName()).orElse(null);
      if (t == null)
        return new Result(r.requestId(), r.toolName(), false, Map.of(), "Tool is not registered");
      if (t.definition().approvalRequired()) {
        String id =
            String.valueOf(r.context().getOrDefault("approvalId", UUID.randomUUID().toString()));
        var decision = approvals.find(id);
        if (decision.isEmpty()) {
          approvals.request(
              new ApprovalRequest(
                  id,
                  String.valueOf(r.context().getOrDefault("userId", "unknown")),
                  r.toolName(),
                  t.definition().description(),
                  r.arguments(),
                  Instant.now().plus(Duration.ofMinutes(15))));
          return new Result(
              r.requestId(),
              r.toolName(),
              false,
              Map.of("approvalId", id, "status", "PENDING"),
              "Approval required");
        }
        if (decision.get().status() != ApprovalStatus.APPROVED)
          return new Result(
              r.requestId(),
              r.toolName(),
              false,
              Map.of("approvalId", id, "status", decision.get().status().name()),
              "Approval required");
      }
      try {
        return t.execute(r);
      } catch (Exception e) {
        return new Result(r.requestId(), r.toolName(), false, Map.of(), "Tool execution failed");
      }
    }
  }
}
