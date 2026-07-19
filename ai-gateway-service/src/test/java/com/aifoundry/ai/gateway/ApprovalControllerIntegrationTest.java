package com.aifoundry.ai.gateway;

import static org.junit.jupiter.api.Assertions.*;

import com.aifoundry.ai.application.tool.ApprovalService;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"ai.foundry.provider.enabled=false", "spring.profiles.active=test"})
@Import(TestProviders.class)
class ApprovalControllerIntegrationTest {
  @Autowired TestRestTemplate rest;
  @Autowired ApprovalService approvals;

  @Test
  void approvesPendingAction() {
    approvals.request(
        new ApprovalService.Request(
            "integration-approval",
            "u",
            "freeze-card",
            "freeze",
            Map.of(),
            Instant.now().plusSeconds(60)));
    var x =
        rest.postForEntity(
            "/api/v1/approvals/integration-approval/approve",
            Map.of("comment", "approved"),
            Map.class);
    assertEquals(HttpStatus.OK, x.getStatusCode());
    assertEquals("APPROVED", x.getBody().get("status"));
  }
}
