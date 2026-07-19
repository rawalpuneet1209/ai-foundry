package com.aifoundry.ai.application.rag;

import java.util.*;

public final class RagContextBuilder {
  private final int max;

  public RagContextBuilder(int max) {
    this.max = max;
  }

  public List<String> build(RetrievalResult r) {
    List<String> out = new ArrayList<>();
    int used = 0;
    for (Chunk chunk : r.chunks()) {
      String s =
          "[source="
              + chunk.metadata().getOrDefault("source", "unknown")
              + ", chunk="
              + chunk.chunkId()
              + "]\n"
              + chunk.content();
      if (used + s.length() > max) break;
      out.add(s);
      used += s.length();
    }
    return List.copyOf(out);
  }
}
