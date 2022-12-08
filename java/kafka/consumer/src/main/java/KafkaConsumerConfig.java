/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class KafkaConsumerConfig {
    private static final long DEFAULT_MESSAGES_COUNT = 10;
    private static final String KAFKA_PREFIX = "KAFKA_";

    private final String topic;
    private final String enableAutoCommit = "false";
    private final Long messageCount;
    private final TracingSystem tracingSystem;

    private static final Map<String, String> USER_CONFIGS = System.getenv()
            .entrySet()
            .stream()
            .filter(map -> map.getKey().startsWith(KAFKA_PREFIX))
            .collect(Collectors.toMap(map -> convertEnvVarToPropertyKey(map.getKey()), map -> map.getValue()));

    public KafkaConsumerConfig(String topic, Long messageCount, TracingSystem tracingSystem) {
        this.topic = topic;
        this.messageCount = messageCount;
        this.tracingSystem = tracingSystem;
    }

    public static KafkaConsumerConfig fromEnv() {
        String topic = System.getenv("STRIMZI_TOPIC");
        Long messageCount = System.getenv("STRIMZI_MESSAGE_COUNT") == null ? DEFAULT_MESSAGES_COUNT : Long.parseLong(System.getenv("STRIMZI_MESSAGE_COUNT"));
        TracingSystem tracingSystem = TracingSystem.forValue(System.getenv().getOrDefault("STRIMZI_TRACING_SYSTEM", ""));

        return new KafkaConsumerConfig(topic, messageCount, tracingSystem);
    }

    public static String convertEnvVarToPropertyKey(String envVar) {
        System.out.println("ENV_VAR is " + envVar);
        return envVar.substring(envVar.indexOf("_") + 1).toLowerCase().replace("_", ".");
    }

    public static Properties createProperties(KafkaConsumerConfig config) {
        Properties props = new Properties();
        props.putAll(USER_CONFIGS);
        return props;
    }

    public String getTopic() {
        return topic;
    }

    public String getEnableAutoCommit() {
        return enableAutoCommit;
    }

    public Long getMessageCount() {
        return messageCount;
    }

    public TracingSystem getTracingSystem() {
        return tracingSystem;
    }

    @Override
    public String toString() {
        return "KafkaConsumerConfig{" +
                "topic='" + topic + '\'' +
                ", enableAutoCommit='" + enableAutoCommit + '\'' +
                ", messageCount=" + messageCount +
                ", tracingSystem='" + tracingSystem + '\'' +
                kafkaConfigOptionsToString() +
                '}';
    }

    public static String kafkaConfigOptionsToString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : USER_CONFIGS.entrySet()) {
            sb.append(", " + entry.getKey() + "='" + entry.getValue() + "\'");
        }
        return sb.toString();
    }
}
