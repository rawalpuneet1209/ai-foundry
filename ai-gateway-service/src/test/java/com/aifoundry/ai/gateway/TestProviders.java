package com.aifoundry.ai.gateway;

import com.aifoundry.ai.domain.chat.*;
import com.aifoundry.ai.domain.embedding.EmbeddingModels;
import com.aifoundry.ai.provider.spi.*;
import java.time.*;
import java.util.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import reactor.core.publisher.Flux;

@TestConfiguration
public class TestProviders {
  @Bean
  ChatProvider chatProvider() {
    return new ChatProvider() {
      public ChatResponse chat(ChatRequest r) {
        return new ChatResponse(
            "test-response",
            r.conversationId(),
            "test-chat",
            "Test response",
            new TokenUsage(1, 2, 3),
            FinishReason.STOP,
            Map.of());
      }

      public org.reactivestreams.Publisher<ChatResponseChunk> stream(ChatRequest r) {
        return Flux.just(
            new ChatResponseChunk(
                "test-response",
                r.conversationId(),
                "test-chat",
                "Test",
                false,
                FinishReason.UNKNOWN,
                Map.of()),
            new ChatResponseChunk(
                "test-response",
                r.conversationId(),
                "test-chat",
                "",
                true,
                FinishReason.STOP,
                Map.of()));
      }

      public String providerName() {
        return "test";
      }

      public Set<String> supportedModels() {
        return Set.of("test-chat");
      }
    };
  }

  @Bean
  EmbeddingProvider embeddingProvider() {
    return new EmbeddingProvider() {
      public EmbeddingModels.Response embed(EmbeddingModels.Request r) {
        List<EmbeddingModels.Embedding> out = new ArrayList<>();
        for (int i = 0; i < r.inputs().size(); i++)
          out.add(new EmbeddingModels.Embedding(i, List.of(1f, (float) (i + 1))));
        return new EmbeddingModels.Response("test-embed", out, TokenUsage.unknown());
      }

      public String providerName() {
        return "test";
      }

      public Set<String> supportedModels() {
        return Set.of("test-embed");
      }
    };
  }

  @Bean
  AiProviderHealthIndicator health() {
    return () -> new ProviderHealth("test", ProviderStatus.UP, Duration.ZERO, Map.of());
  }
}
