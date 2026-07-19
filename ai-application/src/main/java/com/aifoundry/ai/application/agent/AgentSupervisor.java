package com.aifoundry.ai.application.agent;

import com.aifoundry.ai.domain.agent.AgentModels.AgentId;
import com.aifoundry.ai.domain.agent.AgentModels.AgentType;
import com.aifoundry.ai.domain.agent.AgentModels.ExecutionStatus;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AgentSupervisor {
  private final IntentClassifier classifier;
  private final AgentRegistry agents;

  public AgentSupervisor(IntentClassifier classifier, AgentRegistry agents) {
    this.classifier = classifier;
    this.agents = agents;
  }

  public AgentResponse execute(AgentRequest request) {
    String executionId =
        request.executionId() == null || request.executionId().isBlank()
            ? UUID.randomUUID().toString()
            : request.executionId();
    AgentRequest normalized =
        new AgentRequest(
            executionId,
            request.conversationId(),
            request.userId(),
            request.message(),
            request.context());
    AgentType intent = classifier.classify(request.message(), request.context());
    var selected = agents.byType(intent).or(() -> agents.byType(AgentType.GENERAL_BANKING));
    if (selected.isEmpty()) {
      return new AgentResponse(
          executionId,
          new AgentId("supervisor"),
          "No suitable agent is available.",
          ExecutionStatus.FAILED,
          List.of(),
          Map.of("classifiedIntent", intent.name()));
    }
    AgentResponse response = selected.get().execute(normalized);
    Map<String, Object> metadata = new HashMap<>(response.metadata());
    metadata.put("classifiedIntent", intent.name());
    metadata.put("selectedAgent", selected.get().definition().id().value());
    return new AgentResponse(
        response.executionId(),
        response.agentId(),
        response.content(),
        response.status(),
        response.actions(),
        metadata);
  }
}
