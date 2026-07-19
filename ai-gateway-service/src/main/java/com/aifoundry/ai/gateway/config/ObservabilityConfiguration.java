package com.aifoundry.ai.gateway.config;

import com.aifoundry.ai.application.observability.AiMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.*;

@Configuration
public class ObservabilityConfiguration {
  @Bean
  AiMetrics.Chat chatMetrics(MeterRegistry r) {
    return new AiMetrics.MicrometerChat(r);
  }

  @Bean
  AiMetrics.Rag ragMetrics(MeterRegistry r) {
    return new AiMetrics.MicrometerRag(r);
  }

  @Bean
  AiMetrics.Agent agentMetrics(MeterRegistry r) {
    return new AiMetrics.MicrometerAgent(r);
  }

  @Bean
  AiMetrics.Tool toolMetrics(MeterRegistry r) {
    return new AiMetrics.MicrometerTool(r);
  }
}
