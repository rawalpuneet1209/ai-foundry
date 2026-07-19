package com.aifoundry.ai.application.rag;

import static org.junit.jupiter.api.Assertions.*;

import com.aifoundry.ai.domain.rag.RagModels.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class InMemoryVectorStoreAdapterTest {
  @Test
  void ordersByCosineSimilarity() {
    var s = new RagServices.InMemoryVectorStore();
    var id = new DocumentId("d");
    s.upsert(
        List.of(
            new ChunkEmbedding(new DocumentChunk("a", id, "a", 0, 0, 1, Map.of()), List.of(1f, 0f)),
            new ChunkEmbedding(
                new DocumentChunk("b", id, "b", 1, 1, 2, Map.of()), List.of(0f, 1f))));
    var result = s.search(new RetrievalQuery("q", 1, 0d, Map.of()), List.of(1f, 0f));
    assertEquals("a", result.getFirst().chunk().chunkId());
  }
}
