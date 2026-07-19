package com.aifoundry.ai.provider.springai.chat;

import com.aifoundry.ai.domain.chat.ChatResponse;
import com.aifoundry.ai.domain.chat.FinishReason;
import com.aifoundry.ai.domain.chat.TokenUsage;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.ai.chat.metadata.Usage;

public final class SpringAiResponseMapper {
  public ChatResponse map(
      org.springframework.ai.chat.model.ChatResponse response,
      String conversationId,
      String fallbackModel,
      String providerName) {
    var metadata = response.getMetadata();
    Usage usage = metadata.getUsage();
    String responseId = metadata.getId() == null ? UUID.randomUUID().toString() : metadata.getId();
    String model = metadata.getModel() == null ? fallbackModel : metadata.getModel();
    String content =
        response.getResult() == null || response.getResult().getOutput() == null
            ? ""
            : response.getResult().getOutput().getText();
    String finishReason =
        response.getResult() == null || response.getResult().getMetadata() == null
            ? null
            : response.getResult().getMetadata().getFinishReason();
    return new ChatResponse(
        responseId,
        conversationId,
        model,
        content,
        new TokenUsage(
            usage == null ? 0 : value(usage.getPromptTokens()),
            usage == null ? 0 : value(usage.getCompletionTokens()),
            usage == null ? 0 : value(usage.getTotalTokens())),
        finishReason(finishReason),
        Map.of("provider", providerName));
  }

  private int value(Integer value) {
    return value == null ? 0 : value;
  }

  private FinishReason finishReason(String value) {
    if (value == null || value.isBlank()) {
      return FinishReason.UNKNOWN;
    }
    try {
      return FinishReason.valueOf(value.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ignored) {
      return FinishReason.UNKNOWN;
    }
  }
}
