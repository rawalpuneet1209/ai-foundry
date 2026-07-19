package com.aifoundry.ai.application.tool;

import com.aifoundry.platform.common.error.ValidationException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ToolExecutionService {
  private final ToolRegistry tools;
  private final ApprovalService approvals;
  private final PendingToolRequestRepository pendingRequests;

  public ToolExecutionService(
      ToolRegistry tools, ApprovalService approvals, PendingToolRequestRepository pendingRequests) {
    this.tools = tools;
    this.approvals = approvals;
    this.pendingRequests = pendingRequests;
  }

  public ToolResult execute(ToolRequest request, Set<String> allowedTools) {
    if (!allowedTools.contains(request.toolName())) {
      return failed(request, "Tool is not allowed for this agent");
    }
    Tool tool = tools.find(request.toolName()).orElse(null);
    if (tool == null) {
      return failed(request, "Tool is not registered");
    }
    if (tool.definition().approvalRequired()) {
      ToolResult approvalResult = approvalResult(request, tool.definition(), allowedTools);
      if (approvalResult != null) {
        return approvalResult;
      }
    }
    try {
      return tool.execute(request);
    } catch (RuntimeException exception) {
      return failed(request, "Tool execution failed");
    }
  }

  public ToolResult resume(String approvalId) {
    var pending =
        pendingRequests
            .find(approvalId)
            .orElseThrow(() -> new ValidationException("Pending tool request not found"));
    var decision =
        approvals.find(approvalId).orElseThrow(() -> new ValidationException("Approval not found"));
    if (decision.status() != ApprovalService.Status.APPROVED) {
      return approvalRequired(pending.request(), approvalId, decision.status());
    }
    Tool tool =
        tools
            .find(pending.request().toolName())
            .orElseThrow(() -> new ValidationException("Tool is not registered"));
    try {
      ToolResult result = tool.execute(pending.request());
      pendingRequests.remove(approvalId);
      return result;
    } catch (RuntimeException exception) {
      return failed(pending.request(), "Tool execution failed");
    }
  }

  public void discard(String approvalId) {
    pendingRequests.remove(approvalId);
  }

  private ToolResult approvalResult(
      ToolRequest request, ToolDefinition definition, Set<String> allowedTools) {
    String approvalId =
        String.valueOf(request.context().getOrDefault("approvalId", UUID.randomUUID().toString()));
    var decision = approvals.find(approvalId);
    if (decision.isEmpty()) {
      approvals.request(
          new ApprovalService.Request(
              approvalId,
              String.valueOf(request.context().getOrDefault("userId", "unknown")),
              request.toolName(),
              definition.description(),
              request.arguments(),
              Instant.now().plus(Duration.ofMinutes(15))));
      pendingRequests.save(
          approvalId, new PendingToolRequestRepository.PendingRequest(request, allowedTools));
      return approvalRequired(request, approvalId, ApprovalService.Status.PENDING);
    }
    if (decision.get().status() == ApprovalService.Status.APPROVED) {
      return null;
    }
    if (decision.get().status() == ApprovalService.Status.REJECTED) {
      return new ToolResult(
          request.requestId(),
          request.toolName(),
          ToolResult.Status.REJECTED,
          Map.of("approvalId", approvalId, "status", decision.get().status().name()),
          "Approval rejected");
    }
    return approvalRequired(request, approvalId, decision.get().status());
  }

  private ToolResult approvalRequired(
      ToolRequest request, String approvalId, ApprovalService.Status status) {
    return new ToolResult(
        request.requestId(),
        request.toolName(),
        ToolResult.Status.APPROVAL_REQUIRED,
        Map.of("approvalId", approvalId, "status", status.name()),
        "Approval required");
  }

  private ToolResult failed(ToolRequest request, String message) {
    return new ToolResult(
        request.requestId(), request.toolName(), ToolResult.Status.FAILED, Map.of(), message);
  }
}
