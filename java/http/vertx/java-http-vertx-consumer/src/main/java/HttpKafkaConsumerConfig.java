/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import java.util.Optional;

import io.vertx.core.http.HttpClientOptions;
import io.strimzi.common.TracingSystem;

/**
 * HttpKafkaConsumerConfig
 */
public class HttpKafkaConsumerConfig {

    private static final String DEFAULT_HOSTNAME = "localhost";
    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_TOPIC = "test";
    private static final String DEFAULT_CLIENTID = "my-consumer";
    private static final String DEFAULT_GROUPID = "my-group";
    private static final int DEFAULT_POLL_INTERVAL = 1000;
    private static final int DEFAULT_POLL_TIMEOUT = 100;
    private static final boolean DEFAULT_PIPELINING = HttpClientOptions.DEFAULT_PIPELINING;
    private static final int DEFAULT_PIPELINING_LIMIT = HttpClientOptions.DEFAULT_PIPELINING_LIMIT;
    private static final String DEFAULT_ENDPOINT_PREFIX = "";

    private final String hostname;
    private final int port;
    private final String topic;
    private final String clientId;
    private final String groupId;
    private final int pollInterval;
    private final int pollTimeout;
    private final boolean pipelining;
    private final int pipeliningLimit;
    private final Optional<Long> messageCount;
    private final String endpointPrefix;
    private final TracingSystem tracingSystem;

    /**
     * Constructor
     *
     * @param hostname hostname to which connect to
     * @param port host port to which connect to
     * @param topic Kafka topic from which consume messages
     * @param clientId Kafka consumer clientId used as consumer name
     * @param groupId consumer group name the consumer belong to
     * @param pollInterval interval (in ms) for polling to get messages
     * @param pollTimeout timeout (in ms) for polling to get messages
     * @param pipelining if the HTTP client has to pipeline requests
     * @param pipeliningLimit the maximum number of requests in the pipeline
     * @param messageCount number of messages to receive
     * @param endpointPrefix a prefix to use in the endpoint path
     * @param tracingSystem system used to enable tracing
     */
    @SuppressWarnings("checkstyle:ParameterNumber")
    private HttpKafkaConsumerConfig(String hostname, int port,
                                    String topic, String clientId, String groupId,
                                    int pollInterval, int pollTimeout,
                                    boolean pipelining, int pipeliningLimit,
                                    Optional<Long> messageCount,
                                    String endpointPrefix,
                                    TracingSystem tracingSystem) {
        this.hostname = hostname;
        this.port = port;
        this.topic = topic;
        this.clientId = clientId;
        this.groupId = groupId;
        this.pollInterval = pollInterval;
        this.pollTimeout = pollTimeout;
        this.pipelining = pipelining;
        this.pipeliningLimit = pipeliningLimit;
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
     * @return Kafka topic from which consume messages
     */
    public String getTopic() {
        return topic;
    }

    /**
     * @return Kafka consumer clientId used as consumer name
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * @return consumer group name the consumer belong to
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * @return interval (in ms) for polling to get messages
     */
    public int getPollInterval() {
        return pollInterval;
    }

    /**
     * @return timeout (in ms) for polling to get messages
     */
    public int getPollTimeout() {
        return pollTimeout;
    }

    /**
     * @return if the HTTP client has to pipeline requests
     */
    public boolean isPipelining() {
        return pipelining;
    }

    /**
     * @return the maximum number of requests in the pipeline
     */
    public int getPipeliningLimit() {
        return pipeliningLimit;
    }

    /**
     * @return number of messages to receive
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
     * Load all HTTP Kafka consumer configuration parameters from fromEnv method
     * @return HTTP Kafka consumer configuration
     */
    @SuppressWarnings({"CyclomaticComplexity", "NPathComplexity"})
    public static HttpKafkaConsumerConfig fromEnv() {
        String hostName = System.getenv("STRIMZI_HOSTNAME") == null ? DEFAULT_HOSTNAME : System.getenv("STRIMZI_HOSTNAME");
        int port = System.getenv("STRIMZI_PORT") == null ? DEFAULT_PORT : Integer.parseInt(System.getenv("STRIMZI_PORT"));
        String topic = System.getenv("STRIMZI_TOPIC") == null ? DEFAULT_TOPIC : System.getenv("STRIMZI_TOPIC");
        String clientId = System.getenv("STRIMZI_CLIENT_ID") == null ? DEFAULT_CLIENTID : System.getenv("STRIMZI_CLIENT_ID");
        String groupId = System.getenv("STRIMZI_GROUP_ID") == null ? DEFAULT_GROUPID : System.getenv("STRIMZI_GROUP_ID");
        int pollInterval = System.getenv("STRIMZI_POLL_INTERVAL") == null ? DEFAULT_POLL_INTERVAL : Integer.parseInt(System.getenv("STRIMZI_POLL_INTERVAL"));
        int pollTimeout = System.getenv("STRIMZI_POLL_TIMEOUT") == null ? DEFAULT_POLL_TIMEOUT : Integer.parseInt(System.getenv("STRIMZI_POLL_TIMEOUT"));
        boolean pipelining = System.getenv("STRIMZI_PIPELINING")  == null ? DEFAULT_PIPELINING : Boolean.parseBoolean(System.getenv("STRIMZI_PIPELINING"));
        int pipeliningLimit = System.getenv("STRIMZI_PIPELINING_LIMIT")  == null ? DEFAULT_PIPELINING_LIMIT : Integer.parseInt(System.getenv("STRIMZI_PIPELINING_LIMIT"));
        Optional<Long> messageCount = System.getenv("STRIMZI_MESSAGE_COUNT") == null ? Optional.empty() : Optional.of(Long.parseLong(System.getenv("STRIMZI_MESSAGE_COUNT")));
        String endpointPrefix = System.getenv("STRIMZI_ENDPOINT_PREFIX") == null ? DEFAULT_ENDPOINT_PREFIX : System.getenv("STRIMZI_ENDPOINT_PREFIX");
        TracingSystem tracingSystem = TracingSystem.forValue(System.getenv().getOrDefault("STRIMZI_TRACING_SYSTEM", ""));
        return new HttpKafkaConsumerConfig(hostName, port, topic, clientId, groupId, pollInterval, pollTimeout, pipelining, pipeliningLimit, messageCount, endpointPrefix, tracingSystem);
    }

    @Override
    public String toString() {
        return "HttpKafkaConsumerConfig(" +
                "hostname=" + this.hostname +
                ",port=" + this.port +
                ",topic=" + this.topic +
                ",clientId=" + this.clientId +
                ",groupId=" + this.groupId +
                ",pollInterval=" + this.pollInterval +
                ",pollTimeout=" + this.pollTimeout +
                ",pipelining=" + this.pipelining + 
                ",pipeliningLimit=" + this.pipeliningLimit +
                ",messageCount=" + (this.messageCount.isPresent() ? this.messageCount.get() : null) +
                ",endpointPrefix=" + this.endpointPrefix +
                ",tracingSystem=" + this.tracingSystem +
                ")";
    }
}