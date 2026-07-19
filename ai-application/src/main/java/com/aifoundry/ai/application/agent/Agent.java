package com.aifoundry.ai.application.agent;

public interface Agent {
  AgentDefinition definition();

  AgentResponse execute(AgentRequest request);
}
