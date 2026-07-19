package com.aifoundry.ai.gateway.config;

import org.springframework.context.annotation.*;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {
  @Bean
  @Profile("local|test")
  SecurityFilterChain local(HttpSecurity h) throws Exception {
    return h.csrf(c -> c.disable()).authorizeHttpRequests(a -> a.anyRequest().permitAll()).build();
  }

  @Bean
  @Profile("secure")
  SecurityFilterChain secure(HttpSecurity h) throws Exception {
    return h.csrf(c -> c.disable())
        .authorizeHttpRequests(
            a ->
                a.requestMatchers("/actuator/health")
                    .permitAll()
                    .requestMatchers("/api/v1/approvals/**")
                    .hasRole("APPROVER")
                    .requestMatchers("/api/v1/knowledge/documents/**")
                    .hasRole("AI_ADMIN")
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(o -> o.jwt(j -> {}))
        .build();
  }
}
