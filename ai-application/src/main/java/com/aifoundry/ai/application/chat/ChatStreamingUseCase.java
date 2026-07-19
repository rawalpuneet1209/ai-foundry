package com.aifoundry.ai.application.chat;

import com.aifoundry.ai.provider.spi.ChatResponseChunk;
import org.reactivestreams.Publisher;

public interface ChatStreamingUseCase {
  Publisher<ChatResponseChunk> stream(ChatCommand command);
}
