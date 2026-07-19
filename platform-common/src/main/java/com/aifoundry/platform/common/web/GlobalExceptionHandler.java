package com.aifoundry.platform.common.web;

import com.aifoundry.platform.common.error.*;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.slf4j.MDC;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final org.slf4j.Logger LOG =
      org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(PlatformException.class)
  ResponseEntity<ApiError> platform(PlatformException e, HttpServletRequest r) {
    HttpStatus s =
        switch (e.errorCode()) {
          case VALIDATION_ERROR, INVALID_PROMPT -> HttpStatus.BAD_REQUEST;
          case PROVIDER_TIMEOUT -> HttpStatus.GATEWAY_TIMEOUT;
          case PROVIDER_UNAVAILABLE, MODEL_NOT_FOUND -> HttpStatus.SERVICE_UNAVAILABLE;
          case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
          case FORBIDDEN -> HttpStatus.FORBIDDEN;
          default -> HttpStatus.UNPROCESSABLE_ENTITY;
        };
    return body(s, e.errorCode(), e.getMessage(), e.details(), r);
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiError> unknown(Exception e, HttpServletRequest r) {
    LOG.error(
        "Unhandled request failure correlationId={}", MDC.get(CorrelationIdFilter.MDC_KEY), e);
    return body(
        HttpStatus.INTERNAL_SERVER_ERROR,
        ErrorCode.INTERNAL_ERROR,
        "An unexpected error occurred",
        java.util.Map.of(),
        r);
  }

  private ResponseEntity<ApiError> body(
      HttpStatus s, ErrorCode c, String m, java.util.Map<String, Object> d, HttpServletRequest r) {
    return ResponseEntity.status(s)
        .body(
            new ApiError(
                Instant.now(),
                s.value(),
                s.getReasonPhrase(),
                c.name(),
                m,
                r.getRequestURI(),
                MDC.get(CorrelationIdFilter.MDC_KEY),
                d));
  }
}
