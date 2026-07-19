package com.aifoundry.platform.common.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public final class CorrelationIdFilter extends OncePerRequestFilter {
  public static final String HEADER_NAME = "X-Correlation-Id", MDC_KEY = "correlationId";

  @Override
  protected void doFilterInternal(
      HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    String id = req.getHeader(HEADER_NAME);
    if (id == null || id.isBlank()) id = UUID.randomUUID().toString();
    MDC.put(MDC_KEY, id);
    res.setHeader(HEADER_NAME, id);
    try {
      chain.doFilter(req, res);
    } finally {
      MDC.remove(MDC_KEY);
    }
  }
}
