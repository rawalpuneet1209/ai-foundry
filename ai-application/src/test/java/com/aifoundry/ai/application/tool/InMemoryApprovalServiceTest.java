package com.aifoundry.ai.application.tool;

import static org.junit.jupiter.api.Assertions.*;

import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class InMemoryApprovalServiceTest {
  @Test
  void approvesPendingRequest() {
    var s = new InMemoryApprovalService();
    s.request(
        new ApprovalService.Request(
            "a", "u", "freeze", "freeze", Map.of(), Instant.now().plusSeconds(60)));
    assertEquals(ApprovalService.Status.APPROVED, s.decide("a", true, "approver", "ok").status());
  }

  @Test
  void expiresOldRequest() {
    var s = new InMemoryApprovalService();
    s.request(
        new ApprovalService.Request(
            "a", "u", "freeze", "freeze", Map.of(), Instant.now().minusSeconds(1)));
    assertEquals(ApprovalService.Status.EXPIRED, s.find("a").orElseThrow().status());
  }
}
