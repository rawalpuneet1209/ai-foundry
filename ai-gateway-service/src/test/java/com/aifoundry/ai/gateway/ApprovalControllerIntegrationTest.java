package com.aifoundry.ai.gateway;

import static org.junit.jupiter.api.Assertions.*;

import com.aifoundry.ai.application.tool.ApprovalService;
import com.aifoundry.ai.application.tool.ToolExecutionService;
import com.aifoundry.ai.application.tool.ToolRequest;
import com.aifoundry.ai.application.tool.ToolResult;
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
  @Autowired ToolExecutionService tools;

  @Test
  void approvesPendingAction() {
    ToolResult pending =
        tools.execute(
            new ToolRequest(
                "integration-request",
                "freeze-card",
                Map.of("cardId", "12345678"),
                Map.of("userId", "u")),
            Set.of("freeze-card"));
    String approvalId = pending.output().get("approvalId").toString();
    var x =
        rest.postForEntity(
            "/api/v1/approvals/" + approvalId + "/approve",
            Map.of("comment", "approved"),
            Map.class);
    assertEquals(HttpStatus.OK, x.getStatusCode());
    Map<?, ?> decision = (Map<?, ?>) x.getBody().get("decision");
    Map<?, ?> toolResult = (Map<?, ?>) x.getBody().get("toolResult");
    assertEquals(ApprovalService.Status.APPROVED.name(), decision.get("status"));
    assertEquals(ToolResult.Status.COMPLETED.name(), toolResult.get("status"));
  }
}
