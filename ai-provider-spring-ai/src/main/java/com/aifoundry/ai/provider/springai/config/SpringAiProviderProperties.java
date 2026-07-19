package com.aifoundry.ai.provider.springai.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("ai.foundry.provider")
public record SpringAiProviderProperties(
    String name,
    String defaultChatModel,
    String defaultEmbeddingModel,
    Duration timeout,
    boolean enabled) {
  public SpringAiProviderProperties {
    if (name == null) name = "ollama";
    if (defaultChatModel == null) defaultChatModel = "llama3.2";
    if (defaultEmbeddingModel == null) defaultEmbeddingModel = "nomic-embed-text";
    if (timeout == null) timeout = Duration.ofSeconds(60);
  }
}
