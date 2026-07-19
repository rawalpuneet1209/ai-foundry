package com.aifoundry.ai.provider.springai.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.aifoundry.ai.domain.chat.ChatMessage;
import com.aifoundry.ai.domain.chat.ChatRole;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;

class SpringAiMessageMapperTest {
  private final SpringAiMessageMapper mapper = new SpringAiMessageMapper();

  @Test
  void mapsEveryMessageRoleIndividually() {
    var messages =
        mapper.map(
            List.of(
                message(ChatRole.SYSTEM),
                message(ChatRole.USER),
                message(ChatRole.ASSISTANT),
                new ChatMessage(ChatRole.TOOL, "content", Map.of("tool", "lookup"))));

    assertInstanceOf(SystemMessage.class, messages.get(0));
    assertInstanceOf(UserMessage.class, messages.get(1));
    assertInstanceOf(AssistantMessage.class, messages.get(2));
    ToolResponseMessage tool = assertInstanceOf(ToolResponseMessage.class, messages.get(3));
    assertEquals("lookup", tool.getResponses().getFirst().name());
  }

  private ChatMessage message(ChatRole role) {
    return new ChatMessage(role, "content", Map.of());
  }
}
