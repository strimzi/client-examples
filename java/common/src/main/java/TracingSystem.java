/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

public enum TracingSystem {
    JAEGER,
    OPENTELEMETRY,
    NONE;

    public static TracingSystem forValue(String value) {
        switch (value) {
            case "jaeger":
                return TracingSystem.JAEGER;
            case "opentelemetry":
                return TracingSystem.OPENTELEMETRY;
            default:
                return TracingSystem.NONE;
        }
    }
}
