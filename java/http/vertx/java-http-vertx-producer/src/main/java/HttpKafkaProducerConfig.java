/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import java.util.Map;
import java.util.Optional;

import io.strimzi.common.TracingSystem;

/**
 * HttpKafkaProducerConfig
 */
public class HttpKafkaProducerConfig {

    private static final String STRIMZI_HOSTNAME = "STRIMZI_HOSTNAME";
    private static final String STRIMZI_PORT = "STRIMZI_PORT";
    private static final String STRIMZI_TOPIC = "STRIMZI_TOPIC";
    private static final String STRIMZI_SEND_INTERVAL = "STRIMZI_SEND_INTERVAL";
    private static final String STRIMZI_MESSAGE_COUNT = "STRIMZI_MESSAGE_COUNT";
    private static final String STRIMZI_ENDPOINT_PREFIX = "STRIMZI_ENDPOINT_PREFIX";

    private static final String DEFAULT_HOSTNAME = "localhost";
    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_TOPIC = "test";
    private static final int DEFAULT_SEND_INTERVAL = 1000;
    private static final String DEFAULT_ENDPOINT_PREFIX = "";

    private final String hostname;
    private final int port;
    private final String topic;
    private final int sendInterval;
    private final Optional<Long> messageCount;
    private final String endpointPrefix;
    private final TracingSystem tracingSystem;

    /**
     * Constructor
     * 
     * @param hostname hostname to which connect to
     * @param port host port to which connect to
     * @param topic Kafka topic from which consume messages
     * @param sendInterval interval (in ms) for sending messages
     * @param messageCount number of messages to sent
     * @param endpointPrefix a prefix to use in the endpoint path
     * @param tracingSystem system used to enable tracing
     */
    private HttpKafkaProducerConfig(String hostname, int port, 
                                    String topic, int sendInterval,
                                    Optional<Long> messageCount,
                                    String endpointPrefix,
                                    TracingSystem tracingSystem) {
        this.hostname = hostname;
        this.port = port;
        this.topic = topic;
        this.sendInterval = sendInterval;
        this.messageCount = messageCount;
        this.endpointPrefix = endpointPrefix;
        this.tracingSystem = tracingSystem;
    }

    /**
     * @return hostname to which connect to
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * @return host port to which connect to
     */
    public int getPort() {
        return port;
    }

    /**
     * @return Kafka topic to send messages to
     */
    public String getTopic() {
        return topic;
    }

    /**
     * @return interval (in ms) for sending messages
     */
    public int getSendInterval() {
        return sendInterval;
    }

    /**
     * @return number of messages to sent
     */
    public Optional<Long> getMessageCount() {
        return messageCount;
    }

    /**
     * @return a prefix to use in the endpoint path
     */
    public String getEndpointPrefix() {
        return endpointPrefix;
    }

    /**
     * @return an option to initialise tracing to openTelemetry
     */
    public TracingSystem getTracingSystem() {
        return tracingSystem;
    }

    /**
     * Load all HTTP Kafka producer configuration parameters from a related map
     * 
     * @param map map from which loading configuration parameters
     * @return HTTP Kafka producer configuration
     */
    public static HttpKafkaProducerConfig fromMap(Map<String, Object> map) {
        String hostname = (String) map.getOrDefault(STRIMZI_HOSTNAME, DEFAULT_HOSTNAME);
        int port = Integer.parseInt(map.getOrDefault(STRIMZI_PORT, DEFAULT_PORT).toString());
        String topic = (String) map.getOrDefault(STRIMZI_TOPIC, DEFAULT_TOPIC);
        int sendInterval = Integer.parseInt(map.getOrDefault(STRIMZI_SEND_INTERVAL, DEFAULT_SEND_INTERVAL).toString());
        String envMessageCount = (String) map.get(STRIMZI_MESSAGE_COUNT);
        Optional<Long> messageCount = envMessageCount != null ? Optional.of(Long.parseLong(envMessageCount)) : Optional.empty();
        String endpointPrefix = (String) map.getOrDefault(STRIMZI_ENDPOINT_PREFIX, DEFAULT_ENDPOINT_PREFIX);
        TracingSystem tracingSystem = TracingSystem.forValue(System.getenv().getOrDefault("STRIMZI_TRACING_SYSTEM", ""));
        return new HttpKafkaProducerConfig(hostname, port, topic, sendInterval, messageCount, endpointPrefix, tracingSystem);
    }

    @Override
    public String toString() {
        return "HttpKafkaProducerConfig(" +
                "hostname=" + this.hostname +
                ",port=" + this.port +
                ",topic=" + this.topic +
                ",sendInterval=" + this.sendInterval +
                ",messageCount=" + (this.messageCount.isPresent() ? this.messageCount.get() : null) +
                ",endpointPrefix=" + this.endpointPrefix +
                ", tracingSystem=" + tracingSystem +
                ")";
    }
}