package com.aifoundry.ai.application.rag;

import com.aifoundry.ai.domain.rag.RagModels.ChunkEmbedding;
import com.aifoundry.ai.domain.rag.RagModels.DocumentId;
import com.aifoundry.ai.domain.rag.RagModels.RetrievalQuery;
import com.aifoundry.ai.domain.rag.RagModels.RetrievedChunk;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryVectorStore implements VectorStore {
  private final ConcurrentMap<String, ChunkEmbedding> embeddings = new ConcurrentHashMap<>();
  private final CosineSimilarity similarity;

  public InMemoryVectorStore() {
    this(new CosineSimilarity());
  }

  public InMemoryVectorStore(CosineSimilarity similarity) {
    this.similarity = Objects.requireNonNull(similarity);
  }

  @Override
  public void upsert(List<ChunkEmbedding> values) {
    values.forEach(value -> embeddings.put(value.chunk().chunkId(), value));
  }

  @Override
  public List<RetrievedChunk> search(RetrievalQuery query, List<Float> vector) {
    return embeddings.values().stream()
        .filter(
            embedding ->
                query.filters().entrySet().stream()
                    .allMatch(
                        filter ->
                            Objects.equals(
                                embedding.chunk().metadata().get(filter.getKey()),
                                filter.getValue())))
        .map(
            embedding ->
                new RetrievedChunk(
                    embedding.chunk(), similarity.calculate(vector, embedding.vector())))
        .filter(result -> result.score() >= query.minimumScore())
        .sorted(Comparator.comparingDouble(RetrievedChunk::score).reversed())
        .limit(query.topK())
        .toList();
  }

  @Override
  public void delete(DocumentId documentId) {
    embeddings
        .entrySet()
        .removeIf(entry -> entry.getValue().chunk().documentId().equals(documentId));
  }

  @Override
  public long count() {
    return embeddings.size();
  }
}
