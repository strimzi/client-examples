/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common;

import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import io.jaegertracing.Configuration;

/**
 * Provides methods for configuring tracing
 */
public class TracingInitializer {
    /**
     Initializes Jaeger Tracing by loading the configuration from the environment
     and registering the tracer as a global tracer
     @return the initialized Jaeger Tracer object
     */
    public static Tracer jaegerInitialize() {
        Tracer tracer = Configuration.fromEnv().getTracer();
        GlobalTracer.registerIfAbsent(tracer);
        return tracer;
    }

    /**
     Initializes OpenTelemetry Tracing by loading the configuration from the environment
     and registering the tracer as a global tracer
     @return the initialized OpenTelemetry Tracer object
     */
    public static void otelInitialize() {
        AutoConfiguredOpenTelemetrySdk.initialize();
    }
}
