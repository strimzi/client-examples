/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import io.strimzi.common.TracingSystem;

import java.util.Optional;

/**
 * HttpKafkaProducerConfig
 */
public class HttpKafkaProducerConfig {

    private static final String DEFAULT_HOSTNAME = "localhost";
    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_TOPIC = "test";
    private static final int DEFAULT_DELAY_MS = 1000;
    private static final String DEFAULT_ENDPOINT_PREFIX = "";

    private final String hostname;
    private final int port;
    private final String topic;
    private final int delay;
    private final Optional<Long> messageCount;
    private final String endpointPrefix;
    private final TracingSystem tracingSystem;

    /**
     * Constructor
     * 
     * @param hostname hostname to which connect to
     * @param port host port to which connect to
     * @param topic Kafka topic from which consume messages
     * @param delay (in ms) between sending messages
     * @param messageCount number of messages to sent
     * @param endpointPrefix a prefix to use in the endpoint path
     * @param tracingSystem system used to enable tracing
     */
    private HttpKafkaProducerConfig(String hostname, int port, 
                                    String topic, int delay,
                                    Optional<Long> messageCount,
                                    String endpointPrefix,
                                    TracingSystem tracingSystem) {
        this.hostname = hostname;
        this.port = port;
        this.topic = topic;
        this.delay = delay;
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
     * @return delay (in ms) between sending messages
     */
    public int getDelay() {
        return delay;
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
     * Load all HTTP Kafka producer configuration parameters from fromEnv method
     * @return HTTP Kafka producer configuration
     */
    public static HttpKafkaProducerConfig fromEnv() {
        String hostName = System.getenv("STRIMZI_HOSTNAME") == null ? DEFAULT_HOSTNAME : System.getenv("STRIMZI_HOSTNAME");
        int port = System.getenv("STRIMZI_PORT") == null ? DEFAULT_PORT : Integer.parseInt(System.getenv("STRIMZI_PORT"));
        String topic = System.getenv("STRIMZI_TOPIC") == null ? DEFAULT_TOPIC : System.getenv("STRIMZI_TOPIC");
        int delay = System.getenv("STRIMZI_DELAY_MS") == null ? DEFAULT_DELAY_MS : Integer.parseInt(System.getenv("STRIMZI_DELAY_MS"));
        Optional<Long> messageCount = System.getenv("STRIMZI_MESSAGE_COUNT") == null ? Optional.empty() : Optional.of(Long.parseLong(System.getenv("STRIMZI_MESSAGE_COUNT")));
        String endpointPrefix = System.getenv("STRIMZI_ENDPOINT_PREFIX") == null ? DEFAULT_ENDPOINT_PREFIX : System.getenv("STRIMZI_ENDPOINT_PREFIX");
        TracingSystem tracingSystem = TracingSystem.forValue(System.getenv().getOrDefault("STRIMZI_TRACING_SYSTEM", ""));
        return new HttpKafkaProducerConfig(hostName, port, topic, delay, messageCount, endpointPrefix, tracingSystem);
    }

    @Override
    public String toString() {
        return "HttpKafkaProducerConfig(" +
                "hostname=" + this.hostname +
                ",port=" + this.port +
                ",topic=" + this.topic +
                ",delay=" + this.delay +
                ",messageCount=" + (this.messageCount.orElse(null)) +
                ",endpointPrefix=" + this.endpointPrefix +
                ",tracingSystem=" + this.tracingSystem +
                ")";
    }
}