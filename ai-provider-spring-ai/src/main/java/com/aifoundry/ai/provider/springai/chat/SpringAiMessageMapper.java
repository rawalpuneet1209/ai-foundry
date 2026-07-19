package com.aifoundry.ai.provider.springai.chat;

import com.aifoundry.ai.domain.chat.ChatMessage;
import java.util.List;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;

public final class SpringAiMessageMapper {
  public List<Message> map(List<ChatMessage> messages) {
    return messages.stream().map(this::map).toList();
  }

  public Message map(ChatMessage message) {
    return switch (message.role()) {
      case SYSTEM -> new SystemMessage(message.content());
      case USER -> new UserMessage(message.content());
      case ASSISTANT -> new AssistantMessage(message.content());
      case TOOL ->
          ToolResponseMessage.builder()
              .responses(
                  List.of(
                      new ToolResponseMessage.ToolResponse(
                          String.valueOf(message.metadata().getOrDefault("toolCallId", "tool")),
                          String.valueOf(message.metadata().getOrDefault("tool", "tool")),
                          message.content())))
              .metadata(message.metadata())
              .build();
    };
  }
}
