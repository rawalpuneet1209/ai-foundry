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
  private final SpringAiMessageMapper messages;
  private final SpringAiResponseMapper responses;

  public SpringAiChatProvider(ChatClient.Builder b, SpringAiProviderProperties p) {
    this(b, p, new SpringAiMessageMapper(), new SpringAiResponseMapper());
  }

  public SpringAiChatProvider(
      ChatClient.Builder builder,
      SpringAiProviderProperties properties,
      SpringAiMessageMapper messages,
      SpringAiResponseMapper responses) {
    client = builder.build();
    props = properties;
    this.messages = messages;
    this.responses = responses;
  }

  public ChatResponse chat(ChatRequest r) {
    String model = r.model() == null ? props.defaultChatModel() : r.model();
    try {
      var response = client.prompt().messages(messages.map(r.messages())).call().chatResponse();
      return responses.map(response, r.conversationId(), model, props.name());
    } catch (Exception e) {
      throw ProviderException.unavailable(props.name(), model, e);
    }
  }

  public Publisher<ChatResponseChunk> stream(ChatRequest r) {
    String id = UUID.randomUUID().toString(),
        model = r.model() == null ? props.defaultChatModel() : r.model();
    try {
      return client.prompt().messages(messages.map(r.messages())).stream()
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

  public String providerName() {
    return props.name();
  }

  public Set<String> supportedModels() {
    return Set.of(props.defaultChatModel());
  }
}
