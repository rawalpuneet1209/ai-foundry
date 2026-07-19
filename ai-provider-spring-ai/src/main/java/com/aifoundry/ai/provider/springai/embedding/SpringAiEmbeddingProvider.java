package com.aifoundry.ai.provider.springai.embedding;

import com.aifoundry.ai.domain.chat.TokenUsage;
import com.aifoundry.ai.domain.embedding.EmbeddingModels;
import com.aifoundry.ai.provider.spi.EmbeddingProvider;
import com.aifoundry.ai.provider.springai.config.SpringAiProviderProperties;
import com.aifoundry.platform.common.error.ProviderException;
import java.util.*;
import org.springframework.ai.embedding.EmbeddingModel;

public final class SpringAiEmbeddingProvider implements EmbeddingProvider {
  private final EmbeddingModel model;
  private final SpringAiProviderProperties props;

  public SpringAiEmbeddingProvider(EmbeddingModel m, SpringAiProviderProperties p) {
    model = m;
    props = p;
  }

  public EmbeddingModels.Response embed(EmbeddingModels.Request r) {
    String name = r.model() == null ? props.defaultEmbeddingModel() : r.model();
    try {
      List<EmbeddingModels.Embedding> out = new ArrayList<>();
      for (int i = 0; i < r.inputs().size(); i++) {
        float[] raw = model.embed(r.inputs().get(i));
        List<Float> v = new ArrayList<>(raw.length);
        for (float f : raw) v.add(f);
        out.add(new EmbeddingModels.Embedding(i, v));
      }
      return new EmbeddingModels.Response(name, out, TokenUsage.unknown());
    } catch (Exception e) {
      throw ProviderException.unavailable(props.name(), name, e);
    }
  }

  public String providerName() {
    return props.name();
  }

  public Set<String> supportedModels() {
    return Set.of(props.defaultEmbeddingModel());
  }
}
