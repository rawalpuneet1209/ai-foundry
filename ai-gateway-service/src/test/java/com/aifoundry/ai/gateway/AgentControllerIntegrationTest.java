package com.aifoundry.ai.gateway;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"ai.foundry.provider.enabled=false", "spring.profiles.active=test"})
@Import(TestProviders.class)
class AgentControllerIntegrationTest {
  @Autowired TestRestTemplate rest;

  @Test
  void routesFraudRequest() {
    var x =
        rest.postForEntity(
            "/api/v1/agents/execute",
            Map.of("userId", "u", "message", "I see an unauthorized payment"),
            Map.class);
    assertEquals(HttpStatus.OK, x.getStatusCode());
    assertEquals("fraud-agent", ((Map<?, ?>) x.getBody().get("agentId")).get("value"));
  }
}
