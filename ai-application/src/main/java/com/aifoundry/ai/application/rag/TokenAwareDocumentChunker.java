package com.aifoundry.ai.application.rag;

import com.aifoundry.ai.domain.rag.RagModels.DocumentChunk;
import com.aifoundry.ai.domain.rag.RagModels.KnowledgeDocument;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class TokenAwareDocumentChunker implements DocumentChunker {
  private final int chunkSize;
  private final int overlap;
  private final int minimumChunkLength;

  public TokenAwareDocumentChunker(int chunkSize, int overlap, int minimumChunkLength) {
    if (chunkSize <= overlap || overlap < 0 || minimumChunkLength < 0) {
      throw new IllegalArgumentException("invalid chunk configuration");
    }
    this.chunkSize = chunkSize;
    this.overlap = overlap;
    this.minimumChunkLength = minimumChunkLength;
  }

  @Override
  public List<DocumentChunk> chunk(KnowledgeDocument document) {
    List<DocumentChunk> chunks = new ArrayList<>();
    String text = document.content();
    for (int start = 0, sequence = 0; start < text.length(); ) {
      int end = Math.min(text.length(), start + chunkSize);
      if (end < text.length()) {
        int boundary = text.lastIndexOf('\n', end);
        if (boundary > start + minimumChunkLength) {
          end = boundary;
        }
      }
      String content = text.substring(start, end).trim();
      if (content.length() >= minimumChunkLength || chunks.isEmpty()) {
        chunks.add(
            new DocumentChunk(
                document.id().value() + "-" + sequence,
                document.id(),
                content,
                sequence,
                start,
                end,
                Map.of("title", document.title(), "source", document.source())));
        sequence++;
      }
      if (end == text.length()) {
        break;
      }
      start = Math.max(start + 1, end - overlap);
    }
    return List.copyOf(chunks);
  }
}
