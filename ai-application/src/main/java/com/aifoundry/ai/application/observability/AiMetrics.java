package com.aifoundry.ai.application.observability;

import io.micrometer.core.instrument.*;
import java.util.function.Supplier;

public final class AiMetrics {
  private AiMetrics() {}

  public interface Chat {
    <T> T record(String provider, String model, Supplier<T> action);

    void error(String provider, String model, Throwable error);
  }

  public interface Rag {
    <T> T ingestion(Supplier<T> a);

    <T> T retrieval(Supplier<T> a);
  }

  public interface Agent {
    <T> T execution(String id, Supplier<T> a);
  }

  public interface Tool {
    <T> T execution(String name, Supplier<T> a);
  }

  private static <T> T timed(MeterRegistry r, String timer, Supplier<T> a, String... tags) {
    return r.timer(timer, tags).record(a);
  }

  public static final class MicrometerChat implements Chat {
    private final MeterRegistry r;

    public MicrometerChat(MeterRegistry r) {
      this.r = r;
    }

    public <T> T record(String p, String m, Supplier<T> a) {
      r.counter("ai_chat_requests_total", "provider", safe(p), "model", safe(m)).increment();
      return timed(r, "ai_chat_latency", a, "provider", safe(p));
    }

    public void error(String p, String m, Throwable e) {
      r.counter(
              "ai_provider_errors_total",
              "provider",
              safe(p),
              "category",
              e == null ? "unknown" : e.getClass().getSimpleName())
          .increment();
    }
  }

  public static final class MicrometerRag implements Rag {
    private final MeterRegistry r;

    public MicrometerRag(MeterRegistry r) {
      this.r = r;
    }

    public <T> T ingestion(Supplier<T> a) {
      T x = timed(r, "ai_rag_ingestion", a);
      r.counter("ai_rag_documents_ingested_total").increment();
      return x;
    }

    public <T> T retrieval(Supplier<T> a) {
      return timed(r, "ai_rag_retrieval_latency", a);
    }
  }

  public static final class MicrometerAgent implements Agent {
    private final MeterRegistry r;

    public MicrometerAgent(MeterRegistry r) {
      this.r = r;
    }

    public <T> T execution(String id, Supplier<T> a) {
      r.counter("ai_agent_executions_total", "agent", safe(id)).increment();
      return timed(r, "ai_agent_execution_latency", a, "agent", safe(id));
    }
  }

  public static final class MicrometerTool implements Tool {
    private final MeterRegistry r;

    public MicrometerTool(MeterRegistry r) {
      this.r = r;
    }

    public <T> T execution(String n, Supplier<T> a) {
      r.counter("ai_tool_executions_total", "tool", safe(n)).increment();
      return timed(r, "ai_tool_execution_latency", a, "tool", safe(n));
    }
  }

  private static String safe(String s) {
    return s == null || s.isBlank() ? "default" : s;
  }
}
