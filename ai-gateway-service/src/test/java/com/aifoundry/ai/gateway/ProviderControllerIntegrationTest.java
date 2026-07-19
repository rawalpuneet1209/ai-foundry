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
class ProviderControllerIntegrationTest {
  @Autowired TestRestTemplate rest;

  @Test
  void hidesInternalConfiguration() {
    var x = rest.getForEntity("/api/v1/providers", String.class);
    assertEquals(HttpStatus.OK, x.getStatusCode());
    assertFalse(x.getBody().contains("base-url"));
  }
}
