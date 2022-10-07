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