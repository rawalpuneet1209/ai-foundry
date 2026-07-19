package com.aifoundry.ai.application.tool;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;

class ToolExecutionServiceTest {
  @Test
  void deniesUnlistedTool() {
    var r = new DefaultToolRegistry();
    r.register(new BankingTools.AccountSummaryTool());
    var result =
        service(r, new InMemoryApprovalService())
            .execute(
                new ToolRequest("r", "account-summary", Map.of("accountId", "1234"), Map.of()),
                Set.of());
    assertFalse(result.success());
  }

  @Test
  void masksAccount() {
    var r = new DefaultToolRegistry();
    r.register(new BankingTools.AccountSummaryTool());
    var result =
        service(r, new InMemoryApprovalService())
            .execute(
                new ToolRequest("r", "account-summary", Map.of("accountId", "12345678"), Map.of()),
                Set.of("account-summary"));
    assertEquals("****5678", result.output().get("accountId"));
  }

  @Test
  void returnsApprovalRequiredBeforeProtectedToolRuns() {
    var registry = new DefaultToolRegistry();
    registry.register(new BankingTools.FreezeCardTool());

    var approvals = new InMemoryApprovalService();
    var service = service(registry, approvals);
    var result =
        service.execute(
            new ToolRequest(
                "request", "freeze-card", Map.of("cardId", "12345678"), Map.of("userId", "user")),
            Set.of("freeze-card"));

    assertEquals(ToolResult.Status.APPROVAL_REQUIRED, result.status());
    assertTrue(result.output().containsKey("approvalId"));
    String approvalId = result.output().get("approvalId").toString();
    approvals.decide(approvalId, true, "approver", "approved");
    ToolResult resumed = service.resume(approvalId);
    assertEquals(ToolResult.Status.COMPLETED, resumed.status());
    assertEquals("****5678", resumed.output().get("cardId"));
  }

  private ToolExecutionService service(ToolRegistry registry, ApprovalService approvals) {
    return new ToolExecutionService(
        registry, approvals, new InMemoryPendingToolRequestRepository());
  }
}
