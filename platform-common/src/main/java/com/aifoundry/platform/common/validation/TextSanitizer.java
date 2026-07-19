package com.aifoundry.platform.common.validation;

import com.aifoundry.platform.common.error.ValidationException;

public final class TextSanitizer {
  public String sanitize(String input) {
    return sanitize(input, Integer.MAX_VALUE);
  }

  public String sanitize(String input, int max) {
    if (input == null) throw new ValidationException("Text is required");
    String s = input.replaceAll("[\\t\\x0B\\f\\r ]+", " ").replaceAll(" *\\n *", "\n").trim();
    if (s.length() > max) throw new ValidationException("Text exceeds maximum length of " + max);
    return s;
  }
}
