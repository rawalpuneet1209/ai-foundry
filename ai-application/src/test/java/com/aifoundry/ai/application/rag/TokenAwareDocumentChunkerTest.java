package com.aifoundry.ai.application.rag;

import static org.junit.jupiter.api.Assertions.*;

import com.aifoundry.ai.domain.rag.RagModels.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class TokenAwareDocumentChunkerTest {
  @Test
  void chunksDeterministicallyWithOffsets() {
    String text =
        "First paragraph has enough text.\nSecond paragraph has enough text.\nThird paragraph.";
    var d =
        new KnowledgeDocument(
            new DocumentId("doc"), "T", text, "s", "text/plain", Map.of(), Instant.now());
    var c = new RagServices.TokenAwareDocumentChunker(45, 5, 10).chunk(d);
    assertTrue(c.size() > 1);
    assertEquals("doc-0", c.getFirst().chunkId());
    assertTrue(c.stream().allMatch(x -> x.endOffset() > x.startOffset()));
  }
}
