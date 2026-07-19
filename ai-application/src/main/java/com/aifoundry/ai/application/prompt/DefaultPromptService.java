package com.aifoundry.ai.application.prompt;

import com.aifoundry.ai.application.rag.RagContextBuilder;
import com.aifoundry.ai.application.rag.RetrievalService;
import com.aifoundry.ai.domain.chat.ChatMessage;
import com.aifoundry.ai.domain.chat.ChatRequest;
import com.aifoundry.ai.domain.chat.ChatRole;
import com.aifoundry.ai.domain.rag.RagModels.RetrievalQuery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DefaultPromptService implements PromptService {
  private final PromptTemplateRepository templates;
  private final PromptBuilder builder;
  private final RetrievalService retrieval;
  private final RagContextBuilder contextBuilder;

  public DefaultPromptService(
      PromptTemplateRepository templates,
      PromptBuilder builder,
      RetrievalService retrieval,
      RagContextBuilder contextBuilder) {
    this.templates = templates;
    this.builder = builder;
    this.retrieval = retrieval;
    this.contextBuilder = contextBuilder;
  }

  @Override
  public ChatRequest build(Request request) {
    String requestedTemplateId =
        request.templateId() == null || request.templateId().isBlank()
            ? "chat-default"
            : request.templateId();
    String templateId =
        request.useRag() && "chat-default".equals(requestedTemplateId)
            ? "rag-banking"
            : requestedTemplateId;
    List<String> retrievedContext = List.of();
    Map<String, Object> metadata = new HashMap<>(request.metadata());
    if (request.useRag()) {
      var result = retrieval.retrieve(new RetrievalQuery(request.question(), 5, 0.0, Map.of()));
      retrievedContext = contextBuilder.build(result);
      metadata.put("retrievalQueryId", result.queryId());
      metadata.put("retrievedChunks", result.chunks().size());
    }

    PromptModels.Context context =
        new PromptModels.Context(
            Map.of("question", request.question()),
            request.conversation(),
            retrievedContext,
            metadata);
    PromptModels.Rendered rendered = builder.build(templates.getRequired(templateId), context);
    List<ChatMessage> messages = new ArrayList<>();
    messages.add(
        new ChatMessage(
            ChatRole.SYSTEM,
            rendered.content(),
            Map.of("templateId", rendered.templateId(), "version", rendered.templateVersion())));
    messages.addAll(request.conversation());
    messages.add(new ChatMessage(ChatRole.USER, request.question(), request.metadata()));
    return new ChatRequest(
        request.conversationId(),
        request.model(),
        messages,
        request.options(),
        Map.copyOf(metadata));
  }
}
