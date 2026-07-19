package com.aifoundry.platform.common.validation;

import java.util.List;

public record ValidationResult(boolean valid, List<String> errors) {
  public ValidationResult {
    errors = List.copyOf(errors);
  }

  public static ValidationResult success() {
    return new ValidationResult(true, List.of());
  }

  public static ValidationResult failure(List<String> e) {
    return new ValidationResult(false, e);
  }
}
