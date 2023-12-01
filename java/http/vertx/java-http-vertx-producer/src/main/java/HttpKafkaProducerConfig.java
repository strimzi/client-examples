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

    private static final String ENV_HOSTNAME = "HOSTNAME";
    private static final String ENV_PORT = "PORT";
    private static final String ENV_TOPIC = "TOPIC";
    private static final String ENV_SEND_INTERVAL = "SEND_INTERVAL";
    private static final String ENV_MESSAGE_COUNT = "MESSAGE_COUNT";
    private static final String ENV_ENDPOINT_PREFIX = "ENDPOINT_PREFIX";

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

    /**
     * Constructor
     * 
     * @param hostname hostname to which connect to
     * @param port host port to which connect to
     * @param topic Kafka topic from which consume messages
     * @param sendInterval interval (in ms) for sending messages
     * @param messageCount number of messages to sent
     * @param endpointPrefix a prefix to use in the endpoint path
     */
    private HttpKafkaProducerConfig(String hostname, int port, 
                                    String topic, int sendInterval,
                                    Optional<Long> messageCount,
                                    String endpointPrefix) {
        this.hostname = hostname;
        this.port = port;
        this.topic = topic;
        this.sendInterval = sendInterval;
        this.messageCount = messageCount;
        this.endpointPrefix = endpointPrefix;
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
     * Load all HTTP Kafka producer configuration parameters from a related map
     * 
     * @param map map from which loading configuration parameters
     * @return HTTP Kafka producer configuration
     */
    public static HttpKafkaProducerConfig fromMap(Map<String, Object> map) {
        String hostname = (String) map.getOrDefault(ENV_HOSTNAME, DEFAULT_HOSTNAME);
        int port = Integer.parseInt(map.getOrDefault(ENV_PORT, DEFAULT_PORT).toString());
        String topic = (String) map.getOrDefault(ENV_TOPIC, DEFAULT_TOPIC);
        int sendInterval = Integer.parseInt(map.getOrDefault(ENV_SEND_INTERVAL, DEFAULT_SEND_INTERVAL).toString());
        String envMessageCount = (String) map.get(ENV_MESSAGE_COUNT);
        Optional<Long> messageCount = envMessageCount != null ? Optional.of(Long.parseLong(envMessageCount)) : Optional.empty();
        String endpointPrefix = (String) map.getOrDefault(ENV_ENDPOINT_PREFIX, DEFAULT_ENDPOINT_PREFIX);
        return new HttpKafkaProducerConfig(hostname, port, topic, sendInterval, messageCount, endpointPrefix);
    }

    public static TracingSystem getTracingSystemFromEnv() {
        return TracingSystem.forValue(System.getenv().getOrDefault("STRIMZI_TRACING_SYSTEM", ""));
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
                ")";
    }
}