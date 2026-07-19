package com.aifoundry.ai.provider.springai.chat;

import com.aifoundry.ai.domain.chat.*;
import com.aifoundry.ai.provider.spi.*;
import com.aifoundry.ai.provider.springai.config.SpringAiProviderProperties;
import com.aifoundry.platform.common.error.ProviderException;
import java.util.*;
import org.reactivestreams.Publisher;
import org.springframework.ai.chat.client.ChatClient;
import reactor.core.publisher.Flux;

public final class SpringAiChatProvider implements ChatProvider {
  private final ChatClient client;
  private final SpringAiProviderProperties props;

  public SpringAiChatProvider(ChatClient.Builder b, SpringAiProviderProperties p) {
    client = b.build();
    props = p;
  }

  public ChatResponse chat(ChatRequest r) {
    String model = r.model() == null ? props.defaultChatModel() : r.model();
    try {
      String content = client.prompt().system(system(r)).user(user(r)).call().content();
      return new ChatResponse(
          UUID.randomUUID().toString(),
          r.conversationId(),
          model,
          content,
          TokenUsage.unknown(),
          FinishReason.STOP,
          Map.of("provider", props.name()));
    } catch (Exception e) {
      throw ProviderException.unavailable(props.name(), model, e);
    }
  }

  public Publisher<ChatResponseChunk> stream(ChatRequest r) {
    String id = UUID.randomUUID().toString(),
        model = r.model() == null ? props.defaultChatModel() : r.model();
    try {
      return client.prompt().system(system(r)).user(user(r)).stream()
          .content()
          .map(
              s ->
                  new ChatResponseChunk(
                      id,
                      r.conversationId(),
                      model,
                      s,
                      false,
                      FinishReason.UNKNOWN,
                      Map.of("provider", props.name())))
          .concatWithValues(
              new ChatResponseChunk(
                  id,
                  r.conversationId(),
                  model,
                  "",
                  true,
                  FinishReason.STOP,
                  Map.of("provider", props.name())));
    } catch (Exception e) {
      return Flux.error(ProviderException.unavailable(props.name(), model, e));
    }
  }

  private String system(ChatRequest r) {
    return r.messages().stream()
        .filter(m -> m.role() == ChatRole.SYSTEM)
        .map(ChatMessage::content)
        .findFirst()
        .orElse("Answer clearly. Do not fabricate facts or claim external actions occurred.");
  }

  private String user(ChatRequest r) {
    return r.messages().stream()
        .map(m -> m.role() + ": " + m.content())
        .collect(java.util.stream.Collectors.joining("\n"));
  }

  public String providerName() {
    return props.name();
  }

  public Set<String> supportedModels() {
    return Set.of(props.defaultChatModel());
  }
}
