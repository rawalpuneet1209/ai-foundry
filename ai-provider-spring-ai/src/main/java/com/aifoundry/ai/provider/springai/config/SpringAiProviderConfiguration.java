package com.aifoundry.ai.provider.springai.config;

import com.aifoundry.ai.provider.spi.*;
import com.aifoundry.ai.provider.springai.chat.SpringAiChatProvider;
import com.aifoundry.ai.provider.springai.embedding.SpringAiEmbeddingProvider;
import java.time.*;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

@Configuration
@EnableConfigurationProperties(SpringAiProviderProperties.class)
@ConditionalOnProperty(prefix = "ai.foundry.provider", name = "enabled", matchIfMissing = true)
public class SpringAiProviderConfiguration {
  @Bean
  ChatProvider chatProvider(ChatClient.Builder b, SpringAiProviderProperties p) {
    return new SpringAiChatProvider(b, p);
  }

  @Bean
  EmbeddingProvider embeddingProvider(EmbeddingModel m, SpringAiProviderProperties p) {
    return new SpringAiEmbeddingProvider(m, p);
  }

  @Bean
  AiProviderHealthIndicator providerHealth(ChatProvider p) {
    return () -> {
      long start = System.nanoTime();
      return new ProviderHealth(
          p.providerName(),
          ProviderStatus.UP,
          Duration.ofNanos(System.nanoTime() - start),
          Map.of("models", p.supportedModels()));
    };
  }
}
