package com.aifoundry.ai.application.agent;

import static org.junit.jupiter.api.Assertions.*;

import com.aifoundry.ai.application.prompt.PromptService;
import com.aifoundry.ai.application.tool.*;
import com.aifoundry.ai.domain.agent.AgentModels.*;
import com.aifoundry.ai.domain.chat.*;
import com.aifoundry.ai.provider.spi.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class SpecialistAgentsTest {
  private final AtomicReference<ChatRequest> lastProviderRequest = new AtomicReference<>();
  private final ChatProvider provider =
      new ChatProvider() {
        public ChatResponse chat(ChatRequest r) {
          lastProviderRequest.set(r);
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

  private final AtomicInteger ragRequests = new AtomicInteger();
  private final PromptService prompts =
      request -> {
        if (request.useRag()) {
          ragRequests.incrementAndGet();
        }
        return new ChatRequest(
            request.conversationId(),
            request.model(),
            List.of(new ChatMessage(ChatRole.USER, request.question(), Map.of())),
            request.options(),
            request.metadata());
      };

  private final ToolExecutionService tools =
      new ToolExecutionService(
          new DefaultToolRegistry(),
          new InMemoryApprovalService(),
          new InMemoryPendingToolRequestRepository());
  private final ToolSelector selector = new RuleBasedToolSelector();

  @Test
  void allSpecialistsComplete() {
    List<Agent> agents =
        List.of(
            new GeneralBankingAgent(prompts, selector, tools, provider),
            new FraudAgent(prompts, selector, tools, provider),
            new LoanAgent(prompts, selector, tools, provider),
            new CreditCardAgent(prompts, selector, tools, provider),
            new AccountAgent(prompts, selector, tools, provider),
            new KnowledgeAgent(prompts, selector, tools, provider));
    for (var a : agents)
      assertEquals(
          ExecutionStatus.COMPLETED,
          a.execute(new AgentRequest("e", "c", "u", "help", Map.of())).status());
    assertEquals(agents.size(), ragRequests.get());
  }

  @Test
  void agentSelectsToolAndUsesUniqueActionId() {
    DefaultToolRegistry registry = new DefaultToolRegistry();
    registry.register(new BankingTools.AccountSummaryTool());
    ToolExecutionService executor =
        new ToolExecutionService(
            registry, new InMemoryApprovalService(), new InMemoryPendingToolRequestRepository());
    Agent agent = new AccountAgent(prompts, selector, executor, provider);

    AgentResponse response =
        agent.execute(
            new AgentRequest(
                "execution-id", "conversation", "user", "Show my account balance", Map.of()));

    assertEquals(ExecutionStatus.COMPLETED, response.status());
    assertEquals("account-summary", response.actions().getFirst().toolName());
    assertNotEquals(response.executionId(), response.actions().getFirst().actionId());
    ChatMessage toolContext = lastProviderRequest.get().messages().getFirst();
    assertEquals(ChatRole.SYSTEM, toolContext.role());
    assertTrue(toolContext.content().contains("Confirmed tool result:"));
    assertTrue(toolContext.content().contains("Tool: account-summary"));
  }
}
