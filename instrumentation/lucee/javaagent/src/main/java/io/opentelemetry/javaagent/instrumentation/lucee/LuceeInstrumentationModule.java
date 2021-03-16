package io.opentelemetry.javaagent.instrumentation.lucee;

import static java.util.Arrays.asList;

import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.tooling.InstrumentationModule;
import io.opentelemetry.javaagent.tooling.TypeInstrumentation;
import java.util.List;

@AutoService(InstrumentationModule.class)
public class LuceeInstrumentationModule extends InstrumentationModule {

  public LuceeInstrumentationModule() {
    super("lucee.core", "org.lucee");
  }

  @Override
  public List<TypeInstrumentation> typeInstrumentations() {
    return asList(new LuceeApplicationInstrumentation(), new LuceeCompileInstrumentation(),
        new LuceeDatabaseManagerInstrumentation());
  }

}