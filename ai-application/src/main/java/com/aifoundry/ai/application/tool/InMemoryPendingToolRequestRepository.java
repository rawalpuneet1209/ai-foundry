package com.aifoundry.ai.application.tool;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryPendingToolRequestRepository implements PendingToolRequestRepository {
  private final ConcurrentMap<String, PendingRequest> requests = new ConcurrentHashMap<>();

  @Override
  public void save(String approvalId, PendingRequest request) {
    requests.put(approvalId, request);
  }

  @Override
  public Optional<PendingRequest> find(String approvalId) {
    return Optional.ofNullable(requests.get(approvalId));
  }

  @Override
  public void remove(String approvalId) {
    requests.remove(approvalId);
  }
}
