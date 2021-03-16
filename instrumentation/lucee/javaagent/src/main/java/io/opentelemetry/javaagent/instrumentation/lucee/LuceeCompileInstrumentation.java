package io.opentelemetry.javaagent.instrumentation.lucee;

import static io.opentelemetry.javaagent.tooling.bytebuddy.matcher.ClassLoaderMatcher.hasClassesNamed;

import static io.opentelemetry.javaagent.instrumentation.api.Java8BytecodeBridge.currentContext;
import static java.util.Collections.singletonMap;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static io.opentelemetry.javaagent.instrumentation.lucee.LuceeTracer.tracer;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.javaagent.tooling.TypeInstrumentation;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import java.util.Map;
import lucee.runtime.PageSource;
import net.bytebuddy.asm.Advice;
import javax.annotation.Nullable;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class LuceeCompileInstrumentation implements TypeInstrumentation {
  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return named("lucee.runtime.compiler.CFMLCompilerImpl");
  }

  @Override
  public Map<? extends ElementMatcher<? super MethodDescription>, String> transformers() {
    return singletonMap(isMethod().and(named("_compile")),
        LuceeCompileInstrumentation.class.getName() + "$CfCompileAdvice");
  }

  public static class CfCompileAdvice {

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void methodEnter(@Advice.Argument(value = 1) @Nullable PageSource ps,
        @Advice.Argument(value = 3) @Nullable String className, @Advice.Local("otelContext") Context context,
        @Advice.Local("otelScope") Scope scope) {

      context = tracer().startSpan("Compilation of  " + ps.getRealpathWithVirtual(), SpanKind.INTERNAL);
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