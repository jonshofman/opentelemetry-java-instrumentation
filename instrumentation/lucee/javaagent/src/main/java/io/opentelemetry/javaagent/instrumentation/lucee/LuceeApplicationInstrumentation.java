package io.opentelemetry.javaagent.instrumentation.lucee;

import static io.opentelemetry.javaagent.instrumentation.api.Java8BytecodeBridge.currentContext;

import io.opentelemetry.api.trace.SpanKind;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import static java.util.Collections.singletonMap;
import javax.annotation.Nullable;

import static io.opentelemetry.javaagent.instrumentation.lucee.LuceeTracer.tracer;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;

import io.opentelemetry.javaagent.tooling.TypeInstrumentation;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import java.util.Map;

public class LuceeApplicationInstrumentation implements TypeInstrumentation {

  public ElementMatcher<TypeDescription> typeMatcher() {
    return named("lucee.runtime.listener.ModernAppListener");
  }

  @Override
  public Map<? extends ElementMatcher<? super MethodDescription>, String> transformers() {
    return singletonMap(isMethod().and(named("call")).and(takesArguments(5)),
        LuceeApplicationInstrumentation.class.getName() + "$CfApplicationAdvice");
  }

  public static class CfApplicationAdvice {

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void methodEnter(@Advice.Argument(value = 2) @Nullable lucee.runtime.type.Collection.Key eventName,
        @Advice.Local("otelContext") Context context, @Advice.Local("otelScope") Scope scope) {

      if (eventName == null) {
        return;
      }

      String eventStringName = eventName.getString();

      context = tracer().startSpan("Application.cfc " + eventStringName, SpanKind.INTERNAL);
      scope = context.makeCurrent();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void methodExit(@Advice.Thrown Throwable throwable, @Advice.Local("otelContext") Context context,
        @Advice.Local("otelScope") Scope scope) {
      scope.close();

      if (throwable != null) {
        tracer().endExceptionally(context, throwable);
      } else {
        tracer().end(context);
      }
    }
  }
}
