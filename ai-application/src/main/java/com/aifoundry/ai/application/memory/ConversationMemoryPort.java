package com.aifoundry.ai.application.memory;

import com.aifoundry.ai.domain.chat.ChatMessage;
import java.util.List;

public interface ConversationMemoryPort {
  List<ChatMessage> load(String id, int max);

  void append(String id, ChatMessage message);

  void clear(String id);
}
