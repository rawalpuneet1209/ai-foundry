package com.aifoundry.ai.application.mcp;

import com.aifoundry.ai.application.tool.ToolServices.*;
import java.util.*;

public final class NoOpMcpToolGateway implements McpToolGateway {
  public List<Definition> discoverTools() {
    return List.of();
  }

  public Result invoke(Request r) {
    return new Result(r.requestId(), r.toolName(), false, Map.of(), "MCP is disabled");
  }
}
