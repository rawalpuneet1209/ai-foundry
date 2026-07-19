package com.aifoundry.ai.application.rag;

import com.aifoundry.ai.domain.rag.RagModels.*;
import java.util.*;

public final class RagContextBuilder {
  private final int max;

  public RagContextBuilder(int max) {
    this.max = max;
  }

  public List<String> build(RetrievalResult r) {
    List<String> out = new ArrayList<>();
    int used = 0;
    for (RetrievedChunk item : r.chunks()) {
      var c = item.chunk();
      String s =
          "[source="
              + c.metadata().getOrDefault("source", "unknown")
              + ", chunk="
              + c.chunkId()
              + "]\n"
              + c.content();
      if (used + s.length() > max) break;
      out.add(s);
      used += s.length();
    }
    return List.copyOf(out);
  }
}
