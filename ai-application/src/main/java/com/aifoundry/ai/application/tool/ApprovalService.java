package com.aifoundry.ai.application.tool;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public interface ApprovalService {
  enum Status {
    PENDING,
    APPROVED,
    REJECTED,
    EXPIRED
  }

  record Request(
      String approvalId,
      String userId,
      String action,
      String description,
      Map<String, Object> payload,
      Instant expiresAt) {
    public Request {
      payload = payload == null ? Map.of() : Map.copyOf(payload);
    }
  }

  record Decision(
      String approvalId, Status status, String decidedBy, Instant decidedAt, String comment) {}

  Decision request(Request request);

  Optional<Decision> find(String approvalId);

  Decision decide(String approvalId, boolean approved, String decidedBy, String comment);
}
