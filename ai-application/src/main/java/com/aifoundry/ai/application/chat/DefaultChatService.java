package com.aifoundry.ai.application.chat;

import com.aifoundry.ai.application.memory.*;
import com.aifoundry.ai.domain.chat.*;
import com.aifoundry.ai.provider.spi.*;
import java.util.*;
import org.reactivestreams.Publisher;

public final class DefaultChatService implements ChatUseCase, ChatStreamingUseCase {
  private final ChatProvider provider;
  private final ConversationMemoryService memory;
  private final ChatCommandValidator validator;

  public DefaultChatService(ChatProvider p, ConversationMemoryService m, ChatCommandValidator v) {
    provider = p;
    memory = m;
    validator = v;
  }

  private ChatRequest request(ChatCommand c, boolean stream) {
    validator.validate(c);
    String id =
        c.conversationId() == null || c.conversationId().isBlank()
            ? UUID.randomUUID().toString()
            : c.conversationId();
    List<ChatMessage> messages = new ArrayList<>(memory.getHistory(id));
    messages.add(new ChatMessage(ChatRole.USER, c.message(), c.metadata()));
    ChatOptions o =
        new ChatOptions(
            c.options().temperature(),
            c.options().topP(),
            c.options().maxTokens(),
            c.options().stopSequences(),
            stream);
    return new ChatRequest(id, c.model(), messages, o, c.metadata());
  }

  public ChatResponse execute(ChatCommand c) {
    ChatRequest r = request(c, false);
    memory.saveUserMessage(r.conversationId(), c.message());
    ChatResponse response = provider.chat(r);
    memory.saveAssistantMessage(r.conversationId(), response.content());
    return response;
  }

  public Publisher<ChatResponseChunk> stream(ChatCommand c) {
    ChatRequest r = request(c, true);
    memory.saveUserMessage(r.conversationId(), c.message());
    return provider.stream(r);
  }
}
