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

public abstract class AbstractBankingAgent implements Agent {
  private final AgentDefinition definition;
  private final String promptTemplateId;
  private final PromptService prompts;
  private final ToolSelector toolSelector;
  private final ToolExecutionService tools;
  private final ChatProvider chat;

  protected AbstractBankingAgent(
      PromptService prompts,
      ToolSelector toolSelector,
      ToolExecutionService tools,
      ChatProvider chat,
      String id,
      String name,
      AgentType type,
      String promptTemplateId,
      Set<String> allowedTools,
      boolean approvalEnabled) {
    this.prompts = prompts;
    this.toolSelector = toolSelector;
    this.tools = tools;
    this.chat = chat;
    this.promptTemplateId = promptTemplateId;
    definition =
        new AgentDefinition(
            new AgentId(id),
            name,
            type,
            name + " specialist",
            Set.of("CHAT", "SAFE_GUIDANCE", "RAG"),
            allowedTools,
            approvalEnabled);
  }

  @Override
  public final AgentDefinition definition() {
    return definition;
  }

  @Override
  public final AgentResponse execute(AgentRequest request) {
    if (request.message() == null || request.message().isBlank()) {
      return response(
          request, "A message is required.", ExecutionStatus.FAILED, List.of(), Map.of());
    }
    List<Action> actions = new ArrayList<>();
    try {
      ChatRequest prompt =
          prompts.build(
              new PromptService.Request(
                  request.conversationId(),
                  promptTemplateId,
                  null,
                  request.message(),
                  ChatOptions.defaults(),
                  true,
                  List.of(),
                  Map.of("agentId", definition.id().value())));

      var selection =
          toolSelector.select(request.message(), request.context(), definition.allowedTools());
      if (selection.isPresent()) {
        ToolResult toolResult = execute(selection.get(), request);
        actions.add(
            new Action(
                toolResult.requestId(),
                toolResult.toolName(),
                selection.get().arguments(),
                toolResult.output(),
                actionStatus(toolResult.status())));
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

  private ToolResult execute(ToolSelector.Selection selection, AgentRequest request) {
    String requestId = UUID.randomUUID().toString();
    return tools.execute(
        new ToolRequest(
            requestId,
            selection.toolName(),
            selection.arguments(),
            Map.of(
                "userId", String.valueOf(request.userId()),
                "executionId", request.executionId(),
                "conversationId", String.valueOf(request.conversationId()))),
        definition.allowedTools());
  }

  private ChatRequest withToolResult(ChatRequest prompt, ToolResult result) {
    List<ChatMessage> messages = new ArrayList<>(prompt.messages());
    messages.add(
        new ChatMessage(
            ChatRole.TOOL, result.output().toString(), Map.of("tool", result.toolName())));
    return new ChatRequest(
        prompt.conversationId(), prompt.model(), messages, prompt.options(), prompt.metadata());
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
