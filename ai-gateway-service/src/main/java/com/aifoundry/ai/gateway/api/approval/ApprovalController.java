package com.aifoundry.ai.gateway.api.approval;

import com.aifoundry.ai.application.tool.ToolServices.*;
import com.aifoundry.platform.common.error.ValidationException;
import java.security.Principal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/approvals")
public class ApprovalController {
  public record DecisionRequest(String comment) {}

  private final ApprovalService approvals;

  public ApprovalController(ApprovalService a) {
    approvals = a;
  }

  @GetMapping("/{id}")
  public ApprovalDecision get(@PathVariable String id) {
    return approvals.find(id).orElseThrow(() -> new ValidationException("Approval not found"));
  }

  @PostMapping("/{id}/approve")
  public ApprovalDecision approve(
      @PathVariable String id, @RequestBody(required = false) DecisionRequest r, Principal p) {
    return approvals.decide(id, true, actor(p), r == null ? null : r.comment());
  }

  @PostMapping("/{id}/reject")
  public ApprovalDecision reject(
      @PathVariable String id, @RequestBody(required = false) DecisionRequest r, Principal p) {
    return approvals.decide(id, false, actor(p), r == null ? null : r.comment());
  }

  private String actor(Principal p) {
    return p == null ? "local-approver" : p.getName();
  }
}
