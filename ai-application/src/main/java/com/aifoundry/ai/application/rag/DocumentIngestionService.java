package com.aifoundry.ai.application.rag;

import com.aifoundry.ai.domain.embedding.EmbeddingModels;
import com.aifoundry.ai.domain.rag.RagModels.*;
import com.aifoundry.ai.provider.spi.EmbeddingProvider;
import com.aifoundry.platform.common.error.*;
import java.time.*;
import java.util.*;

public final class DocumentIngestionService {
  public record Command(
      String documentId,
      String title,
      String content,
      String source,
      String contentType,
      Map<String, Object> metadata,
      boolean overwrite) {}

  public record Result(
      DocumentId documentId, int chunksCreated, Duration duration, String status) {}

  private final DocumentRepositoryPort documents;
  private final RagServices.DocumentChunker chunker;
  private final EmbeddingProvider embeddings;
  private final RagServices.VectorStore vectors;

  public DocumentIngestionService(
      DocumentRepositoryPort d,
      RagServices.DocumentChunker c,
      EmbeddingProvider e,
      RagServices.VectorStore v) {
    documents = d;
    chunker = c;
    embeddings = e;
    vectors = v;
  }

  public Result ingest(Command c) {
    long start = System.nanoTime();
    if (c.documentId() == null
        || c.documentId().isBlank()
        || c.content() == null
        || c.content().isBlank())
      throw new ValidationException("documentId and content are required");
    DocumentId id = new DocumentId(c.documentId());
    if (documents.exists(id) && !c.overwrite())
      throw new ValidationException("Document already exists", Map.of("documentId", id.value()));
    if (c.overwrite()) vectors.delete(id);
    KnowledgeDocument document =
        new KnowledgeDocument(
            id, c.title(), c.content(), c.source(), c.contentType(), c.metadata(), Instant.now());
    documents.save(document);
    try {
      List<DocumentChunk> chunks = chunker.chunk(document);
      var response =
          embeddings.embed(
              new EmbeddingModels.Request(
                  null,
                  chunks.stream().map(DocumentChunk::content).toList(),
                  Map.of("documentId", id.value())));
      if (response.embeddings().size() != chunks.size())
        throw new IllegalStateException("Embedding count mismatch");
      List<ChunkEmbedding> values = new ArrayList<>();
      for (int i = 0; i < chunks.size(); i++)
        values.add(new ChunkEmbedding(chunks.get(i), response.embeddings().get(i).vector()));
      vectors.upsert(values);
      return new Result(
          id, chunks.size(), Duration.ofNanos(System.nanoTime() - start), "COMPLETED");
    } catch (Exception e) {
      documents.delete(id);
      vectors.delete(id);
      throw new RagFailure(
          ErrorCode.RAG_INGESTION_FAILED, "Document ingestion failed", id.value(), e);
    }
  }

  public void delete(String id) {
    DocumentId value = new DocumentId(id);
    if (!documents.exists(value))
      throw new RagFailure(ErrorCode.RAG_DOCUMENT_NOT_FOUND, "Document not found", id, null);
    documents.delete(value);
    vectors.delete(value);
  }
}
