package com.aifoundry.ai.application.agent;

import com.aifoundry.ai.domain.agent.AgentModels.AgentType;
import java.util.Map;

public interface IntentClassifier {
  AgentType classify(String message, Map<String, Object> context);
}
