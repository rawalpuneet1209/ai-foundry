package com.aifoundry.ai.application.agent;

import com.aifoundry.ai.application.prompt.PromptService;
import com.aifoundry.ai.application.tool.ToolExecutionService;
import com.aifoundry.ai.domain.agent.AgentModels.AgentType;
import com.aifoundry.ai.provider.spi.ChatProvider;
import java.util.Set;

public final class AccountAgent extends AbstractBankingAgent {
  public AccountAgent(
      PromptService prompts, ToolSelector selector, ToolExecutionService tools, ChatProvider chat) {
    super(
        prompts,
        selector,
        tools,
        chat,
        "account-agent",
        "Account",
        AgentType.ACCOUNT,
        "agent-account",
        Set.of("account-summary", "transaction-lookup"),
        false);
  }
}
