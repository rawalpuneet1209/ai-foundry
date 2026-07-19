package com.aifoundry.ai.application.agent;

import com.aifoundry.ai.application.prompt.PromptService;
import com.aifoundry.ai.application.tool.ToolExecutionService;
import com.aifoundry.ai.domain.agent.AgentModels.AgentType;
import com.aifoundry.ai.provider.spi.ChatProvider;
import java.util.Set;

public final class GeneralBankingAgent extends AbstractBankingAgent {
  public GeneralBankingAgent(
      PromptService prompts, ToolSelector selector, ToolExecutionService tools, ChatProvider chat) {
    super(
        prompts,
        selector,
        tools,
        chat,
        "general-banking-agent",
        "General Banking",
        AgentType.GENERAL_BANKING,
        "agent-general-banking",
        Set.of(),
        false);
  }
}
