package com.aifoundry.ai.provider.spi;

import com.aifoundry.ai.domain.chat.*;
import java.util.Set;
import org.reactivestreams.Publisher;

public interface ChatProvider {
  ChatResponse chat(ChatRequest request);

  Publisher<ChatResponseChunk> stream(ChatRequest request);

  String providerName();

  Set<String> supportedModels();
}
