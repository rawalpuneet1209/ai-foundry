package com.aifoundry.ai.application.rag;

import com.aifoundry.ai.domain.rag.RagModels.*;
import java.util.Optional;

public interface DocumentRepositoryPort {
  void save(KnowledgeDocument document);

  Optional<KnowledgeDocument> findById(DocumentId id);

  boolean exists(DocumentId id);

  void delete(DocumentId id);
}
