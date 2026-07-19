package com.aifoundry.ai.application.agent;

import com.aifoundry.ai.application.prompt.PromptService;
import com.aifoundry.ai.application.tool.ToolExecutionService;
import com.aifoundry.ai.application.tool.ToolRequest;
import com.aifoundry.ai.application.tool.ToolResult;
import com.aifoundry.ai.domain.agent.AgentModels.Action;
import com.aifoundry.ai.domain.agent.AgentModels.ActionStatus;
import com.aifoundry.ai.domain.agent.AgentModels.AgentId;
import com.aifoundry.ai.domain.agent.AgentModels.AgentType;
import com.aifoundry.ai.domain.agent.AgentModels.ExecutionStatus;
import com.aifoundry.ai.domain.chat.ChatMessage;
import com.aifoundry.ai.domain.chat.ChatOptions;
import com.aifoundry.ai.domain.chat.ChatRequest;
import com.aifoundry.ai.domain.chat.ChatRole;
import com.aifoundry.ai.provider.spi.ChatProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class BankingAgents {
  private BankingAgents() {}

  public abstract static class BankingAgent implements Agent {
    private final AgentDefinition definition;
    private final String promptTemplateId;
    private final boolean retrievalEnabled;
    private final PromptService prompts;
    private final ToolExecutionService tools;
    private final ChatProvider chat;

    protected BankingAgent(
        PromptService prompts,
        ToolExecutionService tools,
        ChatProvider chat,
        String id,
        String name,
        AgentType type,
        String promptTemplateId,
        Set<String> allowedTools,
        boolean retrievalEnabled,
        boolean approvalEnabled) {
      this.prompts = prompts;
      this.tools = tools;
      this.chat = chat;
      this.promptTemplateId = promptTemplateId;
      this.retrievalEnabled = retrievalEnabled;
      definition =
          new AgentDefinition(
              new AgentId(id),
              name,
              type,
              name + " specialist",
              Set.of("CHAT", "SAFE_GUIDANCE"),
              allowedTools,
              approvalEnabled);
    }

    @Override
    public AgentDefinition definition() {
      return definition;
    }

    @Override
    public AgentResponse execute(AgentRequest request) {
      if (request.message() == null || request.message().isBlank()) {
        return response(
            request, "A message is required.", ExecutionStatus.FAILED, List.of(), Map.of());
      }
      List<Action> actions = new ArrayList<>();
      try {
        boolean useRag = retrievalEnabled || Boolean.TRUE.equals(request.context().get("useRag"));
        ChatRequest prompt =
            prompts.build(
                new PromptService.Request(
                    request.conversationId(),
                    promptTemplateId,
                    null,
                    request.message(),
                    ChatOptions.defaults(),
                    useRag,
                    List.of(),
                    Map.of("agentId", definition.id().value())));

        ToolResult toolResult = executeRequestedTool(request);
        if (toolResult != null) {
          ActionStatus actionStatus = actionStatus(toolResult.status());
          actions.add(
              new Action(
                  request.executionId(),
                  toolResult.toolName(),
                  arguments(request.context()),
                  toolResult.output(),
                  actionStatus));
          if (toolResult.status() == ToolResult.Status.APPROVAL_REQUIRED) {
            return response(
                request,
                "Approval is required before this action can be executed.",
                ExecutionStatus.APPROVAL_REQUIRED,
                actions,
                toolResult.output());
          }
          if (!toolResult.success()) {
            return response(
                request,
                toolResult.errorMessage(),
                ExecutionStatus.FAILED,
                actions,
                toolResult.output());
          }
          prompt = withToolResult(prompt, toolResult);
        }

        var result = chat.chat(prompt);
        return response(
            request,
            result.content(),
            ExecutionStatus.COMPLETED,
            actions,
            Map.of("agentType", definition.type().name()));
      } catch (RuntimeException exception) {
        return response(
            request,
            "The assistant could not complete the request safely. Please try again.",
            ExecutionStatus.FAILED,
            actions,
            Map.of("agentType", definition.type().name()));
      }
    }

    private ChatRequest withToolResult(ChatRequest prompt, ToolResult result) {
      List<ChatMessage> messages = new ArrayList<>(prompt.messages());
      messages.add(
          new ChatMessage(
              ChatRole.TOOL, result.output().toString(), Map.of("tool", result.toolName())));
      return new ChatRequest(
          prompt.conversationId(), prompt.model(), messages, prompt.options(), prompt.metadata());
    }

    private ToolResult executeRequestedTool(AgentRequest request) {
      Object toolName = request.context().get("toolName");
      if (toolName == null || toolName.toString().isBlank()) {
        return null;
      }
      Map<String, Object> toolContext =
          request.context().containsKey("approvalId")
              ? Map.of(
                  "userId",
                  String.valueOf(request.userId()),
                  "approvalId",
                  request.context().get("approvalId"))
              : Map.of("userId", String.valueOf(request.userId()));
      return tools.execute(
          new ToolRequest(
              UUID.randomUUID().toString(),
              toolName.toString(),
              arguments(request.context()),
              toolContext),
          definition.allowedTools());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> arguments(Map<String, Object> context) {
      Object arguments = context.get("arguments");
      return arguments instanceof Map<?, ?> ? (Map<String, Object>) arguments : Map.of();
    }

    private ActionStatus actionStatus(ToolResult.Status status) {
      return switch (status) {
        case COMPLETED -> ActionStatus.EXECUTED;
        case APPROVAL_REQUIRED -> ActionStatus.WAITING_APPROVAL;
        case REJECTED -> ActionStatus.REJECTED;
        case FAILED -> ActionStatus.FAILED;
      };
    }

    private AgentResponse response(
        AgentRequest request,
        String content,
        ExecutionStatus status,
        List<Action> actions,
        Map<String, Object> metadata) {
      return new AgentResponse(
          request.executionId(), definition.id(), content, status, actions, metadata);
    }
  }

  public static final class GeneralBankingAgent extends BankingAgent {
    public GeneralBankingAgent(
        PromptService prompts, ToolExecutionService tools, ChatProvider chat) {
      super(
          prompts,
          tools,
          chat,
          "general-banking-agent",
          "General Banking",
          AgentType.GENERAL_BANKING,
          "agent-general-banking",
          Set.of(),
          false,
          false);
    }
  }

  public static final class FraudAgent extends BankingAgent {
    public FraudAgent(PromptService prompts, ToolExecutionService tools, ChatProvider chat) {
      super(
          prompts,
          tools,
          chat,
          "fraud-agent",
          "Fraud",
          AgentType.FRAUD,
          "agent-fraud",
          Set.of("transaction-lookup", "freeze-card"),
          false,
          true);
    }
  }

  public static final class LoanAgent extends BankingAgent {
    public LoanAgent(PromptService prompts, ToolExecutionService tools, ChatProvider chat) {
      super(
          prompts,
          tools,
          chat,
          "loan-agent",
          "Loan",
          AgentType.LOAN,
          "agent-loan",
          Set.of("loan-eligibility-check"),
          true,
          false);
    }
  }

  public static final class CreditCardAgent extends BankingAgent {
    public CreditCardAgent(PromptService prompts, ToolExecutionService tools, ChatProvider chat) {
      super(
          prompts,
          tools,
          chat,
          "credit-card-agent",
          "Credit Card",
          AgentType.CREDIT_CARD,
          "agent-credit-card",
          Set.of("card-details", "card-replacement-request"),
          false,
          true);
    }
  }

  public static final class AccountAgent extends BankingAgent {
    public AccountAgent(PromptService prompts, ToolExecutionService tools, ChatProvider chat) {
      super(
          prompts,
          tools,
          chat,
          "account-agent",
          "Account",
          AgentType.ACCOUNT,
          "agent-account",
          Set.of("account-summary", "transaction-lookup"),
          false,
          false);
    }
  }

  public static final class KnowledgeAgent extends BankingAgent {
    public KnowledgeAgent(PromptService prompts, ToolExecutionService tools, ChatProvider chat) {
      super(
          prompts,
          tools,
          chat,
          "knowledge-agent",
          "Knowledge",
          AgentType.KNOWLEDGE,
          "agent-knowledge",
          Set.of(),
          true,
          false);
    }
  }
}
