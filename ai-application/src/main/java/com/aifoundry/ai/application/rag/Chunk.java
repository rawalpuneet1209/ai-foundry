package com.aifoundry.ai.application.rag;

import java.util.Map;

public record Chunk(
    String chunkId, String documentId, String content, double score, Map<String, Object> metadata) {
  public Chunk {
    metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
  }
}
