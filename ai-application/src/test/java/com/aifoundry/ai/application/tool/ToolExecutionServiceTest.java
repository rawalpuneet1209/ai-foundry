package com.aifoundry.ai.application.tool;

import static org.junit.jupiter.api.Assertions.*;

import com.aifoundry.ai.application.tool.ToolServices.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class ToolExecutionServiceTest {
  @Test
  void deniesUnlistedTool() {
    var r = new Registry();
    r.register(new BankingTools.AccountSummaryTool());
    var result =
        new Executor(r, new ApprovalService())
            .execute(
                new Request("r", "account-summary", Map.of("accountId", "1234"), Map.of()),
                Set.of());
    assertFalse(result.success());
  }

  @Test
  void masksAccount() {
    var r = new Registry();
    r.register(new BankingTools.AccountSummaryTool());
    var result =
        new Executor(r, new ApprovalService())
            .execute(
                new Request("r", "account-summary", Map.of("accountId", "12345678"), Map.of()),
                Set.of("account-summary"));
    assertEquals("****5678", result.output().get("accountId"));
  }
}
