package com.aifoundry.ai.application.rag;

import static org.junit.jupiter.api.Assertions.*;

import com.aifoundry.ai.domain.chat.TokenUsage;
import com.aifoundry.ai.domain.embedding.EmbeddingModels.*;
import com.aifoundry.ai.domain.rag.RagModels.*;
import com.aifoundry.ai.provider.spi.EmbeddingProvider;
import java.util.*;
import org.junit.jupiter.api.Test;

class RetrievalServiceTest {
  @Test
  void embedsAndSearches() {
    EmbeddingProvider e =
        new EmbeddingProvider() {
          public Response embed(Request r) {
            return new Response(
                "m", List.of(new Embedding(0, List.of(1f, 0f))), TokenUsage.unknown());
          }

          public String providerName() {
            return "x";
          }

          public Set<String> supportedModels() {
            return Set.of("m");
          }
        };
    var store = new RagServices.InMemoryVectorStore();
    var id = new DocumentId("d");
    store.upsert(
        List.of(
            new ChunkEmbedding(
                new DocumentChunk("c", id, "content", 0, 0, 7, Map.of()), List.of(1f, 0f))));
    assertEquals(
        1,
        new RetrievalService(e, store, new RetrievalService.NoOpQueryRewriter())
            .retrieve(new RetrievalQuery("query", 5, 0d, Map.of()))
            .chunks()
            .size());
  }
}
