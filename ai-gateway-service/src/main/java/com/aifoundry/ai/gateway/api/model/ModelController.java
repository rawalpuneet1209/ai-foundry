package com.aifoundry.ai.gateway.api.model;

import com.aifoundry.ai.provider.spi.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/models")
public class ModelController {
  private final ChatProvider chat;
  private final EmbeddingProvider embeddings;

  public ModelController(ChatProvider c, EmbeddingProvider e) {
    chat = c;
    embeddings = e;
  }

  @GetMapping
  public Map<String, Object> models() {
    return Map.of("chat", chat.supportedModels(), "embedding", embeddings.supportedModels());
  }
}
