package io.opentelemetry.javaagent.instrumentation.lucee;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.config.Config;
import io.opentelemetry.instrumentation.api.tracer.BaseTracer;
import lucee.runtime.PageSource;
import static io.opentelemetry.api.trace.SpanKind.INTERNAL;

public class LuceeTracer extends BaseTracer {

  private static final LuceeTracer TRACER = new LuceeTracer();

  public static LuceeTracer tracer() {
    return TRACER;
  }

  @Override
  protected String getInstrumentationName() {
    return "io.opentelemetry.lucee";
  }
}