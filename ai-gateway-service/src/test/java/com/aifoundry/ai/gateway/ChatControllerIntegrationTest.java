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
class ChatControllerIntegrationTest {
  @Autowired TestRestTemplate rest;

  @Test
  void returnsContractAndCorrelationId() {
    HttpHeaders h = new HttpHeaders();
    h.setContentType(MediaType.APPLICATION_JSON);
    h.set("X-Correlation-Id", "test-correlation");
    var x =
        rest.exchange(
            "/api/v1/chat/completions",
            HttpMethod.POST,
            new HttpEntity<>(Map.of("message", "hello"), h),
            Map.class);
    assertEquals(HttpStatus.OK, x.getStatusCode());
    assertEquals("Test response", x.getBody().get("content"));
    assertEquals("test-correlation", x.getHeaders().getFirst("X-Correlation-Id"));
  }
}
