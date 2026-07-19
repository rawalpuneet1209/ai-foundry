package com.aifoundry.ai.application.rag;

import com.aifoundry.ai.domain.rag.RagModels.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

public final class RagServices {
  private RagServices() {}

  public interface DocumentChunker {
    List<DocumentChunk> chunk(KnowledgeDocument d);
  }

  public static final class TokenAwareDocumentChunker implements DocumentChunker {
    private final int size, overlap, min;

    public TokenAwareDocumentChunker(int size, int overlap, int min) {
      if (size <= overlap || overlap < 0)
        throw new IllegalArgumentException("invalid chunk configuration");
      this.size = size;
      this.overlap = overlap;
      this.min = min;
    }

    public List<DocumentChunk> chunk(KnowledgeDocument d) {
      List<DocumentChunk> out = new ArrayList<>();
      String text = d.content();
      for (int start = 0, seq = 0; start < text.length(); ) {
        int end = Math.min(text.length(), start + size);
        if (end < text.length()) {
          int boundary = text.lastIndexOf('\n', end);
          if (boundary > start + min) end = boundary;
        }
        String value = text.substring(start, end).trim();
        if (value.length() >= min || out.isEmpty())
          out.add(
              new DocumentChunk(
                  d.id().value() + "-" + seq++,
                  d.id(),
                  value,
                  seq - 1,
                  start,
                  end,
                  Map.of("title", d.title(), "source", d.source())));
        if (end == text.length()) break;
        start = Math.max(start + 1, end - overlap);
      }
      return List.copyOf(out);
    }
  }

  public interface VectorStore {
    void upsert(List<ChunkEmbedding> e);

    List<RetrievedChunk> search(RetrievalQuery q, List<Float> v);

    void delete(DocumentId id);

    long count();
  }

  public static final class InMemoryVectorStore implements VectorStore {
    private final ConcurrentMap<String, ChunkEmbedding> data = new ConcurrentHashMap<>();

    public void upsert(List<ChunkEmbedding> es) {
      es.forEach(e -> data.put(e.chunk().chunkId(), e));
    }

    public List<RetrievedChunk> search(RetrievalQuery q, List<Float> v) {
      return data.values().stream()
          .filter(
              e ->
                  q.filters().entrySet().stream()
                      .allMatch(
                          f -> Objects.equals(e.chunk().metadata().get(f.getKey()), f.getValue())))
          .map(e -> new RetrievedChunk(e.chunk(), cosine(v, e.vector())))
          .filter(r -> r.score() >= q.minimumScore())
          .sorted(Comparator.comparingDouble(RetrievedChunk::score).reversed())
          .limit(q.topK())
          .toList();
    }

    public void delete(DocumentId id) {
      data.entrySet().removeIf(e -> e.getValue().chunk().documentId().equals(id));
    }

    public long count() {
      return data.size();
    }

    private double cosine(List<Float> a, List<Float> b) {
      if (a.size() != b.size() || a.isEmpty()) return 0;
      double dot = 0, aa = 0, bb = 0;
      for (int i = 0; i < a.size(); i++) {
        dot += a.get(i) * b.get(i);
        aa += a.get(i) * a.get(i);
        bb += b.get(i) * b.get(i);
      }
      return aa == 0 || bb == 0 ? 0 : dot / (Math.sqrt(aa) * Math.sqrt(bb));
    }
  }
}
