package com.aifoundry.ai.application.rag;

import com.aifoundry.ai.domain.rag.RagModels.*;
import java.util.*;
import java.util.concurrent.*;

public final class InMemoryDocumentRepositoryAdapter implements DocumentRepositoryPort {
  private final ConcurrentMap<DocumentId, KnowledgeDocument> documents = new ConcurrentHashMap<>();

  public void save(KnowledgeDocument d) {
    documents.put(d.id(), d);
  }

  public Optional<KnowledgeDocument> findById(DocumentId id) {
    return Optional.ofNullable(documents.get(id));
  }

  public boolean exists(DocumentId id) {
    return documents.containsKey(id);
  }

  public void delete(DocumentId id) {
    documents.remove(id);
  }
}
