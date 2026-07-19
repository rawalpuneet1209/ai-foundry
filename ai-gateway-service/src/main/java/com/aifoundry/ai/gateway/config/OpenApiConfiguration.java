package com.aifoundry.ai.gateway.config;

import java.util.*;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfiguration {
  public record ApiDocumentation(
      String title,
      String version,
      String description,
      String securityScheme,
      List<String> groups) {}

  @Bean
  ApiDocumentation apiDocumentation() {
    return new ApiDocumentation(
        "AI Foundry API",
        "v1",
        "Provider-neutral chat, RAG, agents, tools, and approvals",
        "JWT bearer",
        List.of("chat", "knowledge", "agents", "approvals", "providers", "models"));
  }
}
