package com.aifoundry.ai.gateway.api.provider;

import com.aifoundry.ai.provider.spi.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/providers")
public class ProviderController {
  private final ChatProvider chat;
  private final EmbeddingProvider embeddings;
  private final AiProviderHealthIndicator health;

  public ProviderController(ChatProvider c, EmbeddingProvider e, AiProviderHealthIndicator h) {
    chat = c;
    embeddings = e;
    health = h;
  }

  @GetMapping
  public Map<String, Object> providers() {
    return Map.of(
        "provider",
        chat.providerName(),
        "chatModels",
        chat.supportedModels(),
        "embeddingModels",
        embeddings.supportedModels());
  }

  @GetMapping("/health")
  public ProviderHealth health() {
    return health.health();
  }
}
