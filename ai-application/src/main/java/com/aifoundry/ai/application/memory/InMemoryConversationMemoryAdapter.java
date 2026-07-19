package com.aifoundry.ai.application.memory;

import com.aifoundry.ai.domain.chat.ChatMessage;
import java.util.*;
import java.util.concurrent.*;

public final class InMemoryConversationMemoryAdapter implements ConversationMemoryPort {
  private final int capacity;
  private final ConcurrentMap<String, Deque<ChatMessage>> data = new ConcurrentHashMap<>();

  public InMemoryConversationMemoryAdapter(int capacity) {
    this.capacity = capacity;
  }

  public List<ChatMessage> load(String id, int max) {
    if (id == null) return List.of();
    Deque<ChatMessage> d = data.get(id);
    if (d == null) return List.of();
    synchronized (d) {
      return d.stream().skip(Math.max(0, d.size() - max)).toList();
    }
  }

  public void append(String id, ChatMessage m) {
    if (id == null) return;
    Deque<ChatMessage> d = data.computeIfAbsent(id, k -> new ArrayDeque<>());
    synchronized (d) {
      d.addLast(m);
      while (d.size() > capacity) d.removeFirst();
    }
  }

  public void clear(String id) {
    data.remove(id);
  }
}
