package com.aifoundry.ai.domain.embedding;

import com.aifoundry.ai.domain.chat.TokenUsage;
import java.util.*;

public final class EmbeddingModels {
  private EmbeddingModels() {}

  public record Request(String model, List<String> inputs, Map<String, Object> metadata) {
    public Request {
      inputs = List.copyOf(inputs);
      metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
  }

  public record Embedding(int index, List<Float> vector) {
    public Embedding {
      vector = List.copyOf(vector);
    }
  }

  public record Response(String model, List<Embedding> embeddings, TokenUsage tokenUsage) {
    public Response {
      embeddings = List.copyOf(embeddings);
    }
  }
}
