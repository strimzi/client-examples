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
     * Initializes Jaeger Tracing by loading the configuration from the environment
     * and registering the tracer as a global tracer
     *
     * @return a Jaeger Tracer object that is initialised from the environment variables
     */
    public static Tracer jaegerInitialize() {
        Tracer tracer = Configuration.fromEnv().getTracer();
        GlobalTracer.registerIfAbsent(tracer);
        return tracer;
    }

    /**
     * Initializes OpenTelemetry Tracing by loading the configuration from the environment
     * and registering the tracer as a global tracer
     * It sets up the global OpenTelemetry variable with a default SDK instance.
     *
     * @see AutoConfiguredOpenTelemetrySdk
     */
    public static void otelInitialize() {
        AutoConfiguredOpenTelemetrySdk.initialize();
    }
}
