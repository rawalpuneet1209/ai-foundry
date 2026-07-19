package com.aifoundry.ai.gateway.api.approval;

import com.aifoundry.ai.application.tool.ApprovalService;
import com.aifoundry.ai.application.tool.ToolExecutionService;
import com.aifoundry.ai.application.tool.ToolResult;
import com.aifoundry.platform.common.error.ValidationException;
import java.security.Principal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/approvals")
public class ApprovalController {
  public record DecisionRequest(String comment) {}

  public record ApprovalExecutionResponse(
      ApprovalService.Decision decision, ToolResult toolResult) {}

  private final ApprovalService approvals;
  private final ToolExecutionService tools;

  public ApprovalController(ApprovalService approvals, ToolExecutionService tools) {
    this.approvals = approvals;
    this.tools = tools;
  }

  @GetMapping("/{id}")
  public ApprovalService.Decision get(@PathVariable String id) {
    return approvals.find(id).orElseThrow(() -> new ValidationException("Approval not found"));
  }

  @PostMapping("/{id}/approve")
  public ApprovalExecutionResponse approve(
      @PathVariable String id, @RequestBody(required = false) DecisionRequest r, Principal p) {
    ApprovalService.Decision decision =
        approvals.decide(id, true, actor(p), r == null ? null : r.comment());
    return new ApprovalExecutionResponse(decision, tools.resume(id));
  }

  @PostMapping("/{id}/reject")
  public ApprovalExecutionResponse reject(
      @PathVariable String id, @RequestBody(required = false) DecisionRequest r, Principal p) {
    ApprovalService.Decision decision =
        approvals.decide(id, false, actor(p), r == null ? null : r.comment());
    tools.discard(id);
    return new ApprovalExecutionResponse(decision, null);
  }

  private String actor(Principal p) {
    return p == null ? "local-approver" : p.getName();
  }
}
