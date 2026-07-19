package com.aifoundry.platform.common.error;

public final class ProviderException extends PlatformException {
  private ProviderException(ErrorCode c, String m, String p, String model, Throwable t) {
    super(c, m, t == null ? null : t);
  }

  public static ProviderException timeout(String p, String m, Throwable t) {
    return new ProviderException(ErrorCode.PROVIDER_TIMEOUT, "AI provider timed out", p, m, t);
  }

  public static ProviderException unavailable(String p, String m, Throwable t) {
    return new ProviderException(
        ErrorCode.PROVIDER_UNAVAILABLE, "AI provider is unavailable", p, m, t);
  }

  public static ProviderException modelNotFound(String p, String m) {
    return new ProviderException(
        ErrorCode.MODEL_NOT_FOUND, "Requested model is unavailable", p, m, null);
  }

  public static ProviderException unexpected(String p, String m, Throwable t) {
    return new ProviderException(
        ErrorCode.PROVIDER_UNAVAILABLE, "AI provider request failed", p, m, t);
  }
}
