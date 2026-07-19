package com.aifoundry.ai.gateway.config;

import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {
  public void addCorsMappings(CorsRegistry r) {
    r.addMapping("/api/**")
        .allowedOrigins("http://localhost:3000")
        .allowedMethods("GET", "POST", "DELETE")
        .maxAge(3600);
  }
}
