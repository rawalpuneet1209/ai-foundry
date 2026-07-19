package com.aifoundry.ai.provider.springai.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.aifoundry.ai.domain.chat.FinishReason;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.Generation;

class SpringAiResponseMapperTest {
  @Test
  void mapsContentMetadataUsageAndFinishReason() {
    var generation =
        new Generation(
            new AssistantMessage("answer"),
            ChatGenerationMetadata.builder().finishReason("stop").build());
    var source =
        new org.springframework.ai.chat.model.ChatResponse(
            List.of(generation),
            ChatResponseMetadata.builder().id("response").model("model").usage(usage()).build());

    var result = new SpringAiResponseMapper().map(source, "conversation", "fallback", "ollama");

    assertEquals("answer", result.content());
    assertEquals("response", result.responseId());
    assertEquals(6, result.tokenUsage().totalTokens());
    assertEquals(FinishReason.STOP, result.finishReason());
  }

  private Usage usage() {
    return new Usage() {
      @Override
      public Integer getPromptTokens() {
        return 2;
      }

      @Override
      public Integer getCompletionTokens() {
        return 4;
      }

      @Override
      public Object getNativeUsage() {
        return null;
      }
    };
  }
}
