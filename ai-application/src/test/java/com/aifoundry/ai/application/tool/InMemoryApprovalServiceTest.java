package com.aifoundry.ai.application.tool;

import static org.junit.jupiter.api.Assertions.*;

import com.aifoundry.ai.application.tool.ToolServices.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class InMemoryApprovalServiceTest {
  @Test
  void approvesPendingRequest() {
    var s = new ApprovalService();
    s.request(
        new ApprovalRequest("a", "u", "freeze", "freeze", Map.of(), Instant.now().plusSeconds(60)));
    assertEquals(ApprovalStatus.APPROVED, s.decide("a", true, "approver", "ok").status());
  }

  @Test
  void expiresOldRequest() {
    var s = new ApprovalService();
    s.request(
        new ApprovalRequest("a", "u", "freeze", "freeze", Map.of(), Instant.now().minusSeconds(1)));
    assertEquals(ApprovalStatus.EXPIRED, s.find("a").orElseThrow().status());
  }
}
