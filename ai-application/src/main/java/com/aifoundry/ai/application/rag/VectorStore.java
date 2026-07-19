package com.aifoundry.ai.application.rag;

import com.aifoundry.ai.domain.rag.RagModels.ChunkEmbedding;
import com.aifoundry.ai.domain.rag.RagModels.DocumentId;
import com.aifoundry.ai.domain.rag.RagModels.RetrievalQuery;
import com.aifoundry.ai.domain.rag.RagModels.RetrievedChunk;
import java.util.List;

public interface VectorStore {
  void upsert(List<ChunkEmbedding> embeddings);

  List<RetrievedChunk> search(RetrievalQuery query, List<Float> vector);

  void delete(DocumentId documentId);

  long count();
}
