package com.aifoundry.ai.application.agent;

import com.aifoundry.ai.application.prompt.PromptService;
import com.aifoundry.ai.application.tool.ToolExecutionService;
import com.aifoundry.ai.domain.agent.AgentModels.AgentType;
import com.aifoundry.ai.provider.spi.ChatProvider;
import java.util.Set;

public final class LoanAgent extends AbstractBankingAgent {
  public LoanAgent(
      PromptService prompts, ToolSelector selector, ToolExecutionService tools, ChatProvider chat) {
    super(
        prompts,
        selector,
        tools,
        chat,
        "loan-agent",
        "Loan",
        AgentType.LOAN,
        "agent-loan",
        Set.of("loan-eligibility-check"),
        false);
  }
}
