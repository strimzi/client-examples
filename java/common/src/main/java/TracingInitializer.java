/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import io.jaegertracing.Configuration;

public class TracingInitializer {

    public static Tracer jaegerInitialize() {
        Tracer tracer = Configuration.fromEnv().getTracer();
        GlobalTracer.registerIfAbsent(tracer);
        return tracer;
    }

    public static void otelInitialize() {
        AutoConfiguredOpenTelemetrySdk.initialize();
    }
}
