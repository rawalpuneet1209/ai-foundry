package com.aifoundry.ai.application.prompt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aifoundry.ai.application.rag.InMemoryVectorStore;
import com.aifoundry.ai.application.rag.RagContextBuilder;
import com.aifoundry.ai.application.rag.RetrievalService;
import com.aifoundry.ai.domain.chat.ChatOptions;
import com.aifoundry.ai.domain.chat.ChatRole;
import com.aifoundry.ai.domain.chat.TokenUsage;
import com.aifoundry.ai.domain.embedding.EmbeddingModels.Embedding;
import com.aifoundry.ai.domain.embedding.EmbeddingModels.Request;
import com.aifoundry.ai.domain.embedding.EmbeddingModels.Response;
import com.aifoundry.ai.provider.spi.EmbeddingProvider;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class DefaultPromptServiceTest {
  @Test
  void skipsRetrievalWhenRagIsDisabled() {
    AtomicInteger embeddingCalls = new AtomicInteger();
    PromptService service = service(embeddingCalls);

    var request = service.build(request(false));

    assertEquals(0, embeddingCalls.get());
    assertEquals(ChatRole.SYSTEM, request.messages().getFirst().role());
    assertTrue(request.messages().getFirst().content().contains("question"));
    assertFalse(request.metadata().containsKey("retrievalQueryId"));
  }

  @Test
  void performsRetrievalWhenRagIsEnabled() {
    AtomicInteger embeddingCalls = new AtomicInteger();
    PromptService service = service(embeddingCalls);

    var request = service.build(request(true));

    assertEquals(1, embeddingCalls.get());
    assertTrue(request.metadata().containsKey("retrievalQueryId"));
  }

  private PromptService.Request request(boolean useRag) {
    return new PromptService.Request(
        "conversation",
        "chat-default",
        null,
        "question",
        ChatOptions.defaults(),
        useRag,
        List.of(),
        Map.of());
  }

  private PromptService service(AtomicInteger calls) {
    PromptTemplateRepository repository =
        new PromptTemplateRepository() {
          private final Map<String, PromptModels.Template> templates =
              Map.of(
                  "chat-default",
                  template("chat-default", "Answer {{question}}", Set.of("question")),
                  "rag-banking",
                  template(
                      "rag-banking",
                      "Context {{context}} Question {{question}}",
                      Set.of("context", "question")));

          @Override
          public Optional<PromptModels.Template> findById(String id) {
            return Optional.ofNullable(templates.get(id));
          }

          @Override
          public List<PromptModels.Template> findAll() {
            return List.copyOf(templates.values());
          }
        };
    EmbeddingProvider embeddings =
        new EmbeddingProvider() {
          @Override
          public Response embed(Request request) {
            calls.incrementAndGet();
            return new Response(
                "test", List.of(new Embedding(0, List.of(1f))), TokenUsage.unknown());
          }

          @Override
          public String providerName() {
            return "test";
          }

          @Override
          public Set<String> supportedModels() {
            return Set.of("test");
          }
        };
    RetrievalService retrieval =
        new RetrievalService(
            embeddings, new InMemoryVectorStore(), new RetrievalService.NoOpQueryRewriter());
    return new DefaultPromptService(
        repository,
        new PromptBuilder(new PromptRenderer(), new PromptValidator(10_000)),
        retrieval,
        new RagContextBuilder(1_000));
  }

  private PromptModels.Template template(String id, String content, Set<String> variables) {
    return new PromptModels.Template(id, id, "1", content, variables, Map.of());
  }
}
