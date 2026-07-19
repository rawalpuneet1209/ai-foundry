package com.aifoundry.ai.application.prompt;

import com.aifoundry.ai.domain.chat.ChatMessage;
import com.aifoundry.ai.domain.chat.ChatOptions;
import com.aifoundry.ai.domain.chat.ChatRequest;
import java.util.List;
import java.util.Map;

public interface PromptService {
  record Request(
      String conversationId,
      String templateId,
      String model,
      String question,
      ChatOptions options,
      boolean useRag,
      List<ChatMessage> conversation,
      Map<String, Object> metadata) {
    public Request {
      conversation = conversation == null ? List.of() : List.copyOf(conversation);
      metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
      options = options == null ? ChatOptions.defaults() : options;
    }
  }

  ChatRequest build(Request request);
}
