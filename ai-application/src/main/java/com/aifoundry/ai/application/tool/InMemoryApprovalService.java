package com.aifoundry.ai.application.tool;

import com.aifoundry.platform.common.error.ValidationException;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryApprovalService implements ApprovalService {
  private final ConcurrentMap<String, Request> requests = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Decision> decisions = new ConcurrentHashMap<>();

  @Override
  public Decision request(Request request) {
    requests.put(request.approvalId(), request);
    return decisions.computeIfAbsent(
        request.approvalId(), id -> new Decision(id, Status.PENDING, null, null, null));
  }

  @Override
  public Optional<Decision> find(String approvalId) {
    Request request = requests.get(approvalId);
    Decision decision = decisions.get(approvalId);
    if (request != null
        && decision != null
        && decision.status() == Status.PENDING
        && request.expiresAt().isBefore(Instant.now())) {
      decision = new Decision(approvalId, Status.EXPIRED, null, Instant.now(), "Approval expired");
      decisions.put(approvalId, decision);
    }
    return Optional.ofNullable(decision);
  }

  @Override
  public Decision decide(String approvalId, boolean approved, String decidedBy, String comment) {
    if (!requests.containsKey(approvalId)) {
      throw new ValidationException("Approval not found");
    }
    Decision current = find(approvalId).orElseThrow();
    if (current.status() != Status.PENDING) {
      throw new ValidationException("Approval is no longer pending");
    }
    Decision decision =
        new Decision(
            approvalId,
            approved ? Status.APPROVED : Status.REJECTED,
            decidedBy,
            Instant.now(),
            comment);
    decisions.put(approvalId, decision);
    return decision;
  }
}
