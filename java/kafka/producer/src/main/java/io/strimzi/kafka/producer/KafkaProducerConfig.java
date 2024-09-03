/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.kafka.producer;

import io.strimzi.common.ConfigUtil;
import io.strimzi.common.TracingSystem;

import java.util.Properties;

public class KafkaProducerConfig {

    private static final long DEFAULT_MESSAGES_COUNT = 10;
    private static final String DEFAULT_MESSAGE = "Hello world";
    private final String topic;
    private final Long messageCount;
    private final int delay;
    private final String message;
    private final String headers;
    private final TracingSystem tracingSystem;
    private final Properties properties;

    public KafkaProducerConfig(String topic, Long messageCount, int delay, String message,
                               String headers, TracingSystem tracingSystem, Properties properties) {

        this.topic = topic;
        this.messageCount = messageCount;
        this.delay = delay;
        this.message = message;
        this.headers = headers;
        this.tracingSystem = tracingSystem;
        this.properties = properties;
    }

    public static KafkaProducerConfig fromEnv() {
        String topic = System.getenv("STRIMZI_TOPIC");
        Long messageCount = System.getenv("STRIMZI_MESSAGE_COUNT") == null ? DEFAULT_MESSAGES_COUNT : Long.parseLong(System.getenv("STRIMZI_MESSAGE_COUNT"));
        int delay = Integer.parseInt(System.getenv("STRIMZI_DELAY_MS"));
        String message = System.getenv("STRIMZI_MESSAGE") == null ? DEFAULT_MESSAGE : System.getenv("STRIMZI_MESSAGE");
        String headers = System.getenv("STRIMZI_HEADERS");
        TracingSystem tracingSystem = TracingSystem.forValue(System.getenv().getOrDefault("STRIMZI_TRACING_SYSTEM", ""));
        Properties properties = ConfigUtil.getKafkaPropertiesFromEnv();
        return new KafkaProducerConfig(topic, messageCount, delay, message, headers, tracingSystem, properties);
    }

    public String getTopic() {
        return topic;
    }

    public Long getMessageCount() {
        return messageCount;
    }

    public int getDelay() {
        return delay;
    }

    public String getMessage() {
        return message;
    }

    public String getHeaders() {
        return headers;
    }

    public TracingSystem getTracingSystem() {
        return tracingSystem;
    }

    public Properties getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return "KafkaProducerConfig{" +
            ", topic='" + topic + '\'' +
            ", messageCount=" + messageCount +
            ", delay=" + delay +
            ", message=" + message +
            ", headers=" + headers +
            ", tracingSystem='" + tracingSystem + '\'' +
            ", properties ='" + properties + '\'' +
            '}';
    }
}
