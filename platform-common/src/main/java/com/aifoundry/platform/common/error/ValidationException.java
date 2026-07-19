package com.aifoundry.platform.common.error;

import java.util.Map;

public final class ValidationException extends PlatformException {
  public ValidationException(String message) {
    super(ErrorCode.VALIDATION_ERROR, message);
  }

  public ValidationException(String message, Map<String, Object> details) {
    super(ErrorCode.VALIDATION_ERROR, message, details);
  }
}
