/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.kafka.streams;

import io.strimzi.common.ConfigUtil;
import io.strimzi.common.TracingSystem;

import java.util.Properties;

public class KafkaStreamsConfig {

    private final String sourceTopic;
    private final String targetTopic;
    private final TracingSystem tracingSystem;
    private final Properties properties;

    public KafkaStreamsConfig(String sourceTopic, String targetTopic,
                              TracingSystem tracingSystem, Properties properties) {
        this.sourceTopic = sourceTopic;
        this.targetTopic = targetTopic;
        this.tracingSystem = tracingSystem;
        this.properties = properties;
    }

    public static KafkaStreamsConfig fromEnv() {
        String sourceTopic = System.getenv("STRIMZI_SOURCE_TOPIC");
        String targetTopic = System.getenv("STRIMZI_TARGET_TOPIC");
        TracingSystem tracingSystem = TracingSystem.forValue(System.getenv().getOrDefault("STRIMZI_TRACING_SYSTEM", ""));
        Properties properties = ConfigUtil.getKafkaPropertiesFromEnv();
        return new KafkaStreamsConfig(sourceTopic, targetTopic, tracingSystem, properties);
    }

    public String getSourceTopic() {
        return sourceTopic;
    }
    public String getTargetTopic() {
        return targetTopic;
    }
    public TracingSystem getTracingSystem() {
        return tracingSystem;
    }

    public Properties getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return "KafkaStreamsConfig{" +
                ", sourceTopic='" + sourceTopic + '\'' +
                ", targetTopic='" + targetTopic + '\'' +
                ", tracingSystem='" + tracingSystem + '\'' +
                ", properties ='" + properties + '\'' +
                '}';
    }
}
