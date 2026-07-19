package com.aifoundry.ai.application.mcp;

import com.aifoundry.ai.application.tool.ToolServices.*;
import java.util.*;

public interface McpToolGateway {
  List<Definition> discoverTools();

  Result invoke(Request request);
}
