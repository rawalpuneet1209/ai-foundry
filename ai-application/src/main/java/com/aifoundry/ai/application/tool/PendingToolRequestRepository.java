package com.aifoundry.ai.application.tool;

import java.util.Optional;
import java.util.Set;

public interface PendingToolRequestRepository {
  record PendingRequest(ToolRequest request, Set<String> allowedTools) {
    public PendingRequest {
      allowedTools = Set.copyOf(allowedTools);
    }
  }

  void save(String approvalId, PendingRequest request);

  Optional<PendingRequest> find(String approvalId);

  void remove(String approvalId);
}
