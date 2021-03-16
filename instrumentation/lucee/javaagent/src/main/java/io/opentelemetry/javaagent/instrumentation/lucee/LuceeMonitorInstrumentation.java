package io.opentelemetry.javaagent.instrumentation.lucee;

import io.opentelemetry.javaagent.tooling.TypeInstrumentation;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class LuceeMonitorInstrumentation implements TypeInstrumentation {

  @Override
  public ElementMatcher<ClassLoader> classLoaderOptimization() {
    return null;
  }

  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return named("lucee.runtime.engine.Monitor");
  }

  @Override
  public Map<? extends ElementMatcher<? super MethodDescription>, String> transformers() {
    return singletonMap(isMethod().and(named("run")), LuceeMonitorInstrumentation.class.getName() + "$CfMonitorAdvice");
  }

  public static class $CfMonitorAdvice {

  }
}