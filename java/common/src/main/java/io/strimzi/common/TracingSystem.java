/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common;

/**
 * Provides enums to specify which system will be called to initialise tracing
 */
public enum TracingSystem {
    OPENTELEMETRY,
    NONE;

    public static TracingSystem forValue(String value) {
        switch (value) {
            case "opentelemetry":
                return TracingSystem.OPENTELEMETRY;
            default:
                return TracingSystem.NONE;
        }
    }
}
