package com.aifoundry.ai.gateway.api.chat;

import com.aifoundry.ai.application.chat.*;
import com.aifoundry.ai.application.memory.ConversationMemoryService;
import com.aifoundry.ai.domain.chat.*;
import com.aifoundry.ai.provider.spi.ChatResponseChunk;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {
  private final ChatUseCase chat;
  private final ChatStreamingUseCase streaming;
  private final ConversationMemoryService memory;

  public ChatController(ChatUseCase c, ChatStreamingUseCase s, ConversationMemoryService m) {
    chat = c;
    streaming = s;
    memory = m;
  }

  public record Request(
      String conversationId,
      String model,
      @NotBlank String message,
      Double temperature,
      Double topP,
      Integer maxTokens,
      Boolean useRag,
      Map<String, Object> metadata) {}

  public record Usage(long promptTokens, long completionTokens, long totalTokens) {}

  public record Response(
      String responseId,
      String conversationId,
      String model,
      String content,
      Usage usage,
      String finishReason,
      Map<String, Object> metadata) {}

  private ChatCommand command(Request r, boolean stream) {
    return new ChatCommand(
        r.conversationId(),
        null,
        r.model(),
        r.message(),
        new ChatOptions(r.temperature(), r.topP(), r.maxTokens(), List.of(), stream),
        Boolean.TRUE.equals(r.useRag()),
        r.metadata());
  }

  @PostMapping("/completions")
  public Response complete(@Valid @RequestBody Request r) {
    ChatResponse x = chat.execute(command(r, false));
    return new Response(
        x.responseId(),
        x.conversationId(),
        x.model(),
        x.content(),
        new Usage(
            x.tokenUsage().promptTokens(),
            x.tokenUsage().completionTokens(),
            x.tokenUsage().totalTokens()),
        x.finishReason().name(),
        x.metadata());
  }

  @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<ChatResponseChunk> stream(@Valid @RequestBody Request r) {
    return Flux.from(streaming.stream(command(r, true)));
  }

  @DeleteMapping("/conversations/{id}")
  public void clear(@PathVariable String id) {
    memory.clear(id);
  }
}
