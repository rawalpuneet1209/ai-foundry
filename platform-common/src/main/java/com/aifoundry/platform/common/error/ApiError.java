package com.aifoundry.platform.common.error;

import java.time.Instant;
import java.util.Map;

public record ApiError(
    Instant timestamp,
    int status,
    String error,
    String code,
    String message,
    String path,
    String correlationId,
    Map<String, Object> details) {
  public ApiError {
    details = details == null ? Map.of() : Map.copyOf(details);
  }
}
