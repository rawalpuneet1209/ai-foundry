package com.aifoundry.ai.application.mcp;

import com.aifoundry.ai.application.tool.ToolDefinition;
import com.aifoundry.ai.application.tool.ToolRequest;
import com.aifoundry.ai.application.tool.ToolResult;
import java.util.*;

public final class NoOpMcpToolGateway implements McpToolGateway {
  public List<ToolDefinition> discoverTools() {
    return List.of();
  }

  public ToolResult invoke(ToolRequest r) {
    return new ToolResult(
        r.requestId(), r.toolName(), ToolResult.Status.FAILED, Map.of(), "MCP is disabled");
  }
}
