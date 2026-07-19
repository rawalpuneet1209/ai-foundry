package com.aifoundry.ai.gateway.api.rag;

import com.aifoundry.ai.application.rag.*;
import com.aifoundry.ai.domain.rag.RagModels.*;
import com.aifoundry.platform.common.error.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/knowledge")
public class DocumentController {
  public record IngestRequest(
      @NotBlank String documentId,
      String title,
      @NotBlank String content,
      String source,
      String contentType,
      Map<String, Object> metadata,
      Boolean overwrite) {}

  public record IngestResponse(
      String documentId, int chunksCreated, String status, long durationMs) {}

  public record SearchRequest(
      @NotBlank String query, Integer topK, Double minimumScore, Map<String, Object> filters) {}

  public record ChunkDto(
      String chunkId,
      String documentId,
      String content,
      double score,
      Map<String, Object> metadata) {}

  public record SearchResponse(
      String queryId, String query, List<ChunkDto> chunks, long durationMs) {}

  private final DocumentIngestionService ingestion;
  private final DocumentRepositoryPort documents;
  private final RetrievalService retrieval;

  public DocumentController(
      DocumentIngestionService i, DocumentRepositoryPort d, RetrievalService r) {
    ingestion = i;
    documents = d;
    retrieval = r;
  }

  @PostMapping("/documents")
  @ResponseStatus(HttpStatus.CREATED)
  public IngestResponse ingest(@Valid @RequestBody IngestRequest r) {
    var x =
        ingestion.ingest(
            new DocumentIngestionService.Command(
                r.documentId(),
                r.title(),
                r.content(),
                r.source(),
                r.contentType(),
                r.metadata(),
                Boolean.TRUE.equals(r.overwrite())));
    return new IngestResponse(
        x.documentId().value(), x.chunksCreated(), x.status(), x.duration().toMillis());
  }

  @GetMapping("/documents/{id}")
  public KnowledgeDocument get(@PathVariable String id) {
    return documents
        .findById(new DocumentId(id))
        .orElseThrow(
            () -> new RagFailure(ErrorCode.RAG_DOCUMENT_NOT_FOUND, "Document not found", id, null));
  }

  @DeleteMapping("/documents/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable String id) {
    ingestion.delete(id);
  }

  @PostMapping("/search")
  public SearchResponse search(@Valid @RequestBody SearchRequest r) {
    var x =
        retrieval.retrieve(
            new RetrievalQuery(
                r.query(), r.topK() == null ? 5 : r.topK(), r.minimumScore(), r.filters()));
    return new SearchResponse(
        x.queryId(),
        x.query(),
        x.chunks().stream()
            .map(
                c ->
                    new ChunkDto(
                        c.chunk().chunkId(),
                        c.chunk().documentId().value(),
                        c.chunk().content(),
                        c.score(),
                        c.chunk().metadata()))
            .toList(),
        x.duration().toMillis());
  }
}
