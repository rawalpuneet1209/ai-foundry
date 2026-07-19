package com.aifoundry.ai.provider.spi;

import com.aifoundry.ai.domain.embedding.EmbeddingModels;
import java.util.Set;

public interface EmbeddingProvider {
  EmbeddingModels.Response embed(EmbeddingModels.Request request);

  String providerName();

  Set<String> supportedModels();
}
