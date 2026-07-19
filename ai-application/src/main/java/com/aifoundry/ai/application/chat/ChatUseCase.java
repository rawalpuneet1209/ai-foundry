package com.aifoundry.ai.application.chat;

import com.aifoundry.ai.domain.chat.ChatResponse;

public interface ChatUseCase {
  ChatResponse execute(ChatCommand command);
}
