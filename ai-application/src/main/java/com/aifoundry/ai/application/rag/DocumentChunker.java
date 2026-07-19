package com.aifoundry.ai.application.rag;

import com.aifoundry.ai.domain.rag.RagModels.DocumentChunk;
import com.aifoundry.ai.domain.rag.RagModels.KnowledgeDocument;
import java.util.List;

public interface DocumentChunker {
  List<DocumentChunk> chunk(KnowledgeDocument document);
}
