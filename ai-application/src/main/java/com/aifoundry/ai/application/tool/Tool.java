package com.aifoundry.ai.application.tool;

public interface Tool {
  ToolDefinition definition();

  ToolResult execute(ToolRequest request);
}
