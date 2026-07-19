package com.aifoundry.ai.application.agent;

import static org.junit.jupiter.api.Assertions.*;

import com.aifoundry.ai.application.prompt.PromptService;
import com.aifoundry.ai.application.tool.*;
import com.aifoundry.ai.domain.agent.AgentModels.*;
import com.aifoundry.ai.domain.chat.*;
import com.aifoundry.ai.provider.spi.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class SpecialistAgentsTest {
  private final ChatProvider provider =
      new ChatProvider() {
        public ChatResponse chat(ChatRequest r) {
          return new ChatResponse(
              "r",
              r.conversationId(),
              "m",
              "safe answer",
              TokenUsage.unknown(),
              FinishReason.STOP,
              Map.of());
        }

        public org.reactivestreams.Publisher<ChatResponseChunk> stream(ChatRequest r) {
          throw new IllegalStateException();
        }

        public String providerName() {
          return "test";
        }

        public Set<String> supportedModels() {
          return Set.of("m");
        }
      };

  private final PromptService prompts =
      request ->
          new ChatRequest(
              request.conversationId(),
              request.model(),
              List.of(new ChatMessage(ChatRole.USER, request.question(), Map.of())),
              request.options(),
              request.metadata());

  private final ToolExecutionService tools =
      new ToolExecutionService(new DefaultToolRegistry(), new InMemoryApprovalService());

  @Test
  void allSpecialistsComplete() {
    List<Agent> agents =
        List.of(
            new BankingAgents.GeneralBankingAgent(prompts, tools, provider),
            new BankingAgents.FraudAgent(prompts, tools, provider),
            new BankingAgents.LoanAgent(prompts, tools, provider),
            new BankingAgents.CreditCardAgent(prompts, tools, provider),
            new BankingAgents.AccountAgent(prompts, tools, provider),
            new BankingAgents.KnowledgeAgent(prompts, tools, provider));
    for (var a : agents)
      assertEquals(
          ExecutionStatus.COMPLETED,
          a.execute(new AgentRequest("e", "c", "u", "help", Map.of())).status());
  }
}
