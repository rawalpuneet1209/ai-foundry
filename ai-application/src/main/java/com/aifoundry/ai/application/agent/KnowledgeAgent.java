package com.aifoundry.ai.application.agent;

import com.aifoundry.ai.application.prompt.PromptService;
import com.aifoundry.ai.application.tool.ToolExecutionService;
import com.aifoundry.ai.domain.agent.AgentModels.AgentType;
import com.aifoundry.ai.provider.spi.ChatProvider;
import java.util.Set;

public final class KnowledgeAgent extends AbstractBankingAgent {
  public KnowledgeAgent(
      PromptService prompts, ToolSelector selector, ToolExecutionService tools, ChatProvider chat) {
    super(
        prompts,
        selector,
        tools,
        chat,
        "knowledge-agent",
        "Knowledge",
        AgentType.KNOWLEDGE,
        "agent-knowledge",
        Set.of(),
        false);
  }
}
