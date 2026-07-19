package com.aifoundry.ai.application.rag;

import com.aifoundry.platform.common.error.*;

public final class RagFailure extends PlatformException {
  public RagFailure(ErrorCode code, String message, String id, Throwable cause) {
    super(code, message, cause);
    this.id = id;
  }

  private final String id;

  public String id() {
    return id;
  }
}
