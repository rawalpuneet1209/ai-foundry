package com.aifoundry.ai.application.agent;

import static org.junit.jupiter.api.Assertions.*;

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

  @Test
  void allSpecialistsComplete() {
    List<AgentServices.Agent> agents =
        List.of(
            new BankingAgents.GeneralBankingAgent(provider),
            new BankingAgents.FraudAgent(provider),
            new BankingAgents.LoanAgent(provider),
            new BankingAgents.CreditCardAgent(provider),
            new BankingAgents.AccountAgent(provider),
            new BankingAgents.KnowledgeAgent(provider));
    for (var a : agents)
      assertEquals(
          ExecutionStatus.COMPLETED,
          a.execute(new Request("e", "c", "u", "help", Map.of())).status());
  }
}
