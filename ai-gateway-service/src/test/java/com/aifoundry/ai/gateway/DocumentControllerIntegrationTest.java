package com.aifoundry.ai.gateway;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "ai.foundry.provider.enabled=false",
      "spring.profiles.active=test",
      "ai.foundry.rag.minimum-chunk-length=1"
    })
@Import(TestProviders.class)
class DocumentControllerIntegrationTest {
  @Autowired TestRestTemplate rest;

  @Test
  void ingestsAndSearches() {
    var ingest =
        rest.postForEntity(
            "/api/v1/knowledge/documents",
            Map.of(
                "documentId",
                "policy-test",
                "title",
                "Policy",
                "content",
                "Overdraft protection policy",
                "source",
                "test"),
            Map.class);
    assertEquals(HttpStatus.CREATED, ingest.getStatusCode());
    var search =
        rest.postForEntity(
            "/api/v1/knowledge/search", Map.of("query", "overdraft", "topK", 5), Map.class);
    assertEquals(HttpStatus.OK, search.getStatusCode());
    assertFalse(((List<?>) search.getBody().get("chunks")).isEmpty());
    rest.delete("/api/v1/knowledge/documents/policy-test");
  }
}
