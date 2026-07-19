package com.aifoundry.ai.application.rag;

import com.aifoundry.ai.domain.embedding.EmbeddingModels;
import com.aifoundry.ai.domain.rag.RagModels.*;
import com.aifoundry.ai.provider.spi.EmbeddingProvider;
import com.aifoundry.platform.common.error.ValidationException;
import java.time.*;
import java.util.*;

public final class RetrievalService {
  public interface QueryRewriter {
    String rewrite(String query, Map<String, Object> context);
  }

  public static final class NoOpQueryRewriter implements QueryRewriter {
    public String rewrite(String q, Map<String, Object> c) {
      return q;
    }
  }

  private final EmbeddingProvider embeddings;
  private final VectorStore vectors;
  private final QueryRewriter rewriter;

  public RetrievalService(EmbeddingProvider e, VectorStore v, QueryRewriter r) {
    embeddings = e;
    vectors = v;
    rewriter = r;
  }

  public RetrievalResult retrieve(RetrievalQuery q) {
    if (q == null || q.query() == null || q.query().isBlank())
      throw new ValidationException("query is required");
    long start = System.nanoTime();
    String query = rewriter.rewrite(q.query(), q.filters());
    var response = embeddings.embed(new EmbeddingModels.Request(null, List.of(query), Map.of()));
    if (response.embeddings().isEmpty())
      throw new RagFailure(
          com.aifoundry.platform.common.error.ErrorCode.RAG_RETRIEVAL_FAILED,
          "Embedding provider returned no query vector",
          UUID.randomUUID().toString(),
          null);
    var chunks =
        vectors.search(q, response.embeddings().getFirst().vector()).stream()
            .map(
                result ->
                    new Chunk(
                        result.chunk().chunkId(),
                        result.chunk().documentId().value(),
                        result.chunk().content(),
                        result.score(),
                        result.chunk().metadata()))
            .toList();
    return new RetrievalResult(
        UUID.randomUUID().toString(),
        q.query(),
        query,
        chunks,
        Duration.ofNanos(System.nanoTime() - start));
  }
}
