package com.aifoundry.ai.application.rag;

import java.time.Duration;
import java.util.List;

public record RetrievalResult(
    String queryId, String query, String rewrittenQuery, List<Chunk> chunks, Duration duration) {
  public RetrievalResult {
    chunks = List.copyOf(chunks);
  }
}
