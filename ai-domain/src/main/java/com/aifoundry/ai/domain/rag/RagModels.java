package com.aifoundry.ai.domain.rag;

import java.time.*;
import java.util.*;

public final class RagModels {
  private RagModels() {}

  public record DocumentId(String value) {
    public DocumentId {
      if (value == null || value.isBlank())
        throw new IllegalArgumentException("document id required");
    }
  }

  public record KnowledgeDocument(
      DocumentId id,
      String title,
      String content,
      String source,
      String contentType,
      Map<String, Object> metadata,
      Instant createdAt) {
    public KnowledgeDocument {
      Objects.requireNonNull(id);
      if (content == null || content.isBlank())
        throw new IllegalArgumentException("content required");
      metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
      createdAt = createdAt == null ? Instant.now() : createdAt;
    }
  }

  public record DocumentChunk(
      String chunkId,
      DocumentId documentId,
      String content,
      int sequence,
      int startOffset,
      int endOffset,
      Map<String, Object> metadata) {
    public DocumentChunk {
      metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
  }

  public record ChunkEmbedding(DocumentChunk chunk, List<Float> vector) {
    public ChunkEmbedding {
      vector = List.copyOf(vector);
    }
  }

  public record RetrievalQuery(
      String query, int topK, Double minimumScore, Map<String, Object> filters) {
    public RetrievalQuery {
      topK = topK <= 0 ? 5 : topK;
      minimumScore = minimumScore == null ? 0 : minimumScore;
      filters = filters == null ? Map.of() : Map.copyOf(filters);
    }
  }

  public record RetrievedChunk(DocumentChunk chunk, double score) {}

  public record RetrievalResult(
      String queryId, String query, List<RetrievedChunk> chunks, Duration duration) {
    public RetrievalResult {
      chunks = List.copyOf(chunks);
    }
  }
}
