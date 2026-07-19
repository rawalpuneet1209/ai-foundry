package com.aifoundry.ai.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.aifoundry")
public class AiGatewayApplication {
  public static void main(String[] args) {
    SpringApplication.run(AiGatewayApplication.class, args);
  }
}
