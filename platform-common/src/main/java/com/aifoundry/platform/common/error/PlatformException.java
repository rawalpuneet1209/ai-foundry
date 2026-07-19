package com.aifoundry.platform.common.error;

import java.util.Map;

public abstract class PlatformException extends RuntimeException {
  private final ErrorCode errorCode;
  private final Map<String, Object> details;

  protected PlatformException(ErrorCode code, String message) {
    this(code, message, null, Map.of());
  }

  protected PlatformException(ErrorCode code, String message, Throwable cause) {
    this(code, message, cause, Map.of());
  }

  protected PlatformException(ErrorCode code, String message, Map<String, Object> details) {
    this(code, message, null, details);
  }

  private PlatformException(
      ErrorCode code, String message, Throwable cause, Map<String, Object> details) {
    super(message, cause);
    this.errorCode = java.util.Objects.requireNonNull(code);
    this.details = details == null ? Map.of() : Map.copyOf(details);
  }

  public ErrorCode errorCode() {
    return errorCode;
  }

  public Map<String, Object> details() {
    return details;
  }
}
