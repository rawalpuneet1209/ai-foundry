package com.aifoundry.ai.provider.spi;

import java.time.Duration;
import java.util.*;

public record ProviderHealth(
    String provider, ProviderStatus status, Duration latency, Map<String, Object> details) {
  public ProviderHealth {
    details = details == null ? Map.of() : Map.copyOf(details);
  }
}
