package com.aifoundry.ai.application.agent;

import com.aifoundry.ai.application.prompt.PromptService;
import com.aifoundry.ai.application.tool.ToolExecutionService;
import com.aifoundry.ai.domain.agent.AgentModels.AgentType;
import com.aifoundry.ai.provider.spi.ChatProvider;
import java.util.Set;

public final class CreditCardAgent extends AbstractBankingAgent {
  public CreditCardAgent(
      PromptService prompts, ToolSelector selector, ToolExecutionService tools, ChatProvider chat) {
    super(
        prompts,
        selector,
        tools,
        chat,
        "credit-card-agent",
        "Credit Card",
        AgentType.CREDIT_CARD,
        "agent-credit-card",
        Set.of("card-details", "card-replacement-request"),
        true);
  }
}
