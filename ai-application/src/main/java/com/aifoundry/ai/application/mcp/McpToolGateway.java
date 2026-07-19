package com.aifoundry.ai.application.mcp;

import com.aifoundry.ai.application.tool.ToolDefinition;
import com.aifoundry.ai.application.tool.ToolRequest;
import com.aifoundry.ai.application.tool.ToolResult;
import java.util.*;

public interface McpToolGateway {
  List<ToolDefinition> discoverTools();

  ToolResult invoke(ToolRequest request);
}
