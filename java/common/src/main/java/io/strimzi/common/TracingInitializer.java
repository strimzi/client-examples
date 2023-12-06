/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common;

import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;

/**
 * Provides methods for configuring tracing
 */
public class TracingInitializer {
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
