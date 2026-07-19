package com.aifoundry.ai.application.chat;

import com.aifoundry.ai.application.memory.*;
import com.aifoundry.ai.application.prompt.PromptService;
import com.aifoundry.ai.domain.chat.*;
import com.aifoundry.ai.provider.spi.*;
import java.util.*;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public final class DefaultChatService implements ChatUseCase, ChatStreamingUseCase {
  private final ChatProvider provider;
  private final ConversationMemoryService memory;
  private final ChatCommandValidator validator;
  private final PromptService prompts;

  public DefaultChatService(
      ChatProvider p, ConversationMemoryService m, ChatCommandValidator v, PromptService prompts) {
    provider = p;
    memory = m;
    validator = v;
    this.prompts = prompts;
  }

  private ChatRequest request(ChatCommand c, boolean stream) {
    validator.validate(c);
    String id =
        c.conversationId() == null || c.conversationId().isBlank()
            ? UUID.randomUUID().toString()
            : c.conversationId();
    ChatOptions options =
        new ChatOptions(
            c.options().temperature(),
            c.options().topP(),
            c.options().maxTokens(),
            c.options().stopSequences(),
            stream);
    return prompts.build(
        new PromptService.Request(
            id,
            "chat-default",
            c.model(),
            c.message(),
            options,
            c.useRag(),
            memory.getHistory(id),
            c.metadata()));
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
    return persistAssistantResponse(provider.stream(r), r.conversationId());
  }

  private Publisher<ChatResponseChunk> persistAssistantResponse(
      Publisher<ChatResponseChunk> source, String conversationId) {
    return subscriber ->
        source.subscribe(
            new Subscriber<>() {
              private final StringBuilder response = new StringBuilder();

              @Override
              public void onSubscribe(Subscription subscription) {
                subscriber.onSubscribe(subscription);
              }

              @Override
              public void onNext(ChatResponseChunk chunk) {
                response.append(chunk.delta());
                subscriber.onNext(chunk);
              }

              @Override
              public void onError(Throwable throwable) {
                subscriber.onError(throwable);
              }

              @Override
              public void onComplete() {
                if (!response.isEmpty()) {
                  memory.saveAssistantMessage(conversationId, response.toString());
                }
                subscriber.onComplete();
              }
            });
  }
}
