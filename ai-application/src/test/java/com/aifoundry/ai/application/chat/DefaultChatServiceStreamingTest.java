package com.aifoundry.ai.application.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.aifoundry.ai.application.memory.ConversationMemoryService;
import com.aifoundry.ai.application.memory.InMemoryConversationMemoryAdapter;
import com.aifoundry.ai.application.prompt.PromptService;
import com.aifoundry.ai.domain.chat.ChatMessage;
import com.aifoundry.ai.domain.chat.ChatRequest;
import com.aifoundry.ai.domain.chat.ChatResponse;
import com.aifoundry.ai.domain.chat.ChatRole;
import com.aifoundry.ai.domain.chat.FinishReason;
import com.aifoundry.ai.provider.spi.ChatProvider;
import com.aifoundry.ai.provider.spi.ChatResponseChunk;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

class DefaultChatServiceStreamingTest {
  @Test
  void persistsCompletedStreamingResponse() throws InterruptedException {
    var memory = new ConversationMemoryService(new InMemoryConversationMemoryAdapter(10), 10);
    PromptService prompts =
        request ->
            new ChatRequest(
                request.conversationId(),
                request.model(),
                List.of(new ChatMessage(ChatRole.USER, request.question(), Map.of())),
                request.options(),
                request.metadata());
    DefaultChatService service =
        new DefaultChatService(
            new StreamingProvider(), memory, new ChatCommandValidator(100), prompts);
    CountDownLatch completed = new CountDownLatch(1);

    service.stream(new ChatCommand("conversation", "user", null, "hello", null, false, Map.of()))
        .subscribe(subscriber(completed));

    completed.await(1, TimeUnit.SECONDS);
    assertEquals(
        List.of(ChatRole.USER, ChatRole.ASSISTANT),
        memory.getHistory("conversation").stream().map(ChatMessage::role).toList());
    assertEquals("hello world", memory.getHistory("conversation").getLast().content());
  }

  private Subscriber<ChatResponseChunk> subscriber(CountDownLatch completed) {
    return new Subscriber<>() {
      @Override
      public void onSubscribe(Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
      }

      @Override
      public void onNext(ChatResponseChunk chunk) {}

      @Override
      public void onError(Throwable throwable) {
        completed.countDown();
      }

      @Override
      public void onComplete() {
        completed.countDown();
      }
    };
  }

  private static final class StreamingProvider implements ChatProvider {
    @Override
    public ChatResponse chat(ChatRequest request) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Publisher<ChatResponseChunk> stream(ChatRequest request) {
      return subscriber ->
          subscriber.onSubscribe(
              new Subscription() {
                @Override
                public void request(long count) {
                  subscriber.onNext(chunk(request, "hello ", false));
                  subscriber.onNext(chunk(request, "world", true));
                  subscriber.onComplete();
                }

                @Override
                public void cancel() {}
              });
    }

    private ChatResponseChunk chunk(ChatRequest request, String content, boolean completed) {
      return new ChatResponseChunk(
          "response",
          request.conversationId(),
          "test",
          content,
          completed,
          completed ? FinishReason.STOP : FinishReason.UNKNOWN,
          Map.of());
    }

    @Override
    public String providerName() {
      return "test";
    }

    @Override
    public Set<String> supportedModels() {
      return Set.of("test");
    }
  }
}
