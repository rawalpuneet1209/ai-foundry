package com.aifoundry.ai.application.memory;

import com.aifoundry.ai.domain.chat.*;
import java.util.*;

public final class ConversationMemoryService {
  private final ConversationMemoryPort port;
  private final int max;

  public ConversationMemoryService(ConversationMemoryPort p, int max) {
    port = p;
    this.max = max;
  }

  public List<ChatMessage> getHistory(String id) {
    return port.load(id, max);
  }

  public void saveUserMessage(String id, String c) {
    port.append(id, new ChatMessage(ChatRole.USER, c, Map.of()));
  }

  public void saveAssistantMessage(String id, String c) {
    port.append(id, new ChatMessage(ChatRole.ASSISTANT, c, Map.of()));
  }

  public void clear(String id) {
    port.clear(id);
  }
}
