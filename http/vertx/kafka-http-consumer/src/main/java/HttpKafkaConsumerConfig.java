/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import java.util.Map;
import java.util.Optional;

import io.vertx.core.http.HttpClientOptions;

/**
 * HttpKafkaConsumerConfig
 */
public class HttpKafkaConsumerConfig {

    private static final String ENV_HOSTNAME = "HOSTNAME";
    private static final String ENV_PORT = "PORT";
    private static final String ENV_TOPIC = "TOPIC";
    private static final String ENV_GROUPID = "GROUPID";
    private static final String ENV_POLL_INTERVAL = "POLL_INTERVAL";
    private static final String ENV_POLL_TIMEOUT = "POLL_TIMEOUT";
    private static final String ENV_PIPELINING = "PIPELINING";
    private static final String ENV_PIPELINING_LIMIT = "PIPELINING_LIMIT";
    private static final String ENV_MESSAGE_COUNT = "MESSAGE_COUNT";
    private static final String ENV_ENDPOINT_PREFIX = "ENDPOINT_PREFIX";

    private static final String DEFAULT_HOSTNAME = "localhost";
    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_TOPIC = "test";
    private static final String DEFAULT_GROUPID = "my-group";
    private static final int DEFAULT_POLL_INTERVAL = 1000;
    private static final int DEFAULT_POLL_TIMEOUT = 100;
    private static final boolean DEFAULT_PIPELINING = HttpClientOptions.DEFAULT_PIPELINING;
    private static final int DEFAULT_PIPELINING_LIMIT = HttpClientOptions.DEFAULT_PIPELINING_LIMIT;
    private static final String DEFAULT_ENDPOINT_PREFIX = "";

    private final String hostname;
    private final int port;
    private final String topic;
    private final String groupid;
    private final int pollInterval;
    private final int pollTimeout;
    private final boolean pipelining;
    private final int pipeliningLimit;
    private final Optional<Long> messageCount; 
    private final String endpointPrefix;

    /**
     * Constructor
     * 
     * @param hostname hostname to which connect to
     * @param port host port to which connect to
     * @param topic Kafka topic from which consume messages
     * @param groupid consumer group name the consumer belong to
     * @param pollInterval interval (in ms) for polling to get messages
     * @param pollTimeout timeout (in ms) for polling to get messages
     * @param pipelining if the HTTP client has to pipeline requests
     * @param pipeliningLimit the maximum number of requests in the pipeline
     * @param messageCount number of messages to receive
     * @param endpointPrefix a prefix to use in the endpoint path
     */
    private HttpKafkaConsumerConfig(String hostname, int port, 
                                    String topic, String groupid, 
                                    int pollInterval, int pollTimeout,
                                    boolean pipelining, int pipeliningLimit,
                                    Optional<Long> messageCount,
                                    String endpointPrefix) {
        this.hostname = hostname;
        this.port = port;
        this.topic = topic;
        this.groupid = groupid;
        this.pollInterval = pollInterval;
        this.pollTimeout = pollTimeout;
        this.pipelining = pipelining;
        this.pipeliningLimit = pipeliningLimit;
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
     * @return Kafka topic from which consume messages
     */
    public String getTopic() {
        return topic;
    }

    /**
     * @return consumer group name the consumer belong to
     */
    public String getGroupid() {
        return groupid;
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
     * Load all HTTP Kafka consumer configuration parameters from a related map
     * 
     * @param map map from which loading configuration parameters
     * @return HTTP Kafka consumer configuration
     */
    public static HttpKafkaConsumerConfig fromMap(Map<String, Object> map) {
        String hostname = (String) map.getOrDefault(ENV_HOSTNAME, DEFAULT_HOSTNAME);
        int port = Integer.parseInt(map.getOrDefault(ENV_PORT, DEFAULT_PORT).toString());
        String topic = (String) map.getOrDefault(ENV_TOPIC, DEFAULT_TOPIC);
        String groupid = (String) map.getOrDefault(ENV_GROUPID, DEFAULT_GROUPID);
        int pollInterval = Integer.parseInt(map.getOrDefault(ENV_POLL_INTERVAL, DEFAULT_POLL_INTERVAL).toString());
        int pollTimeout = Integer.parseInt(map.getOrDefault(ENV_POLL_TIMEOUT, DEFAULT_POLL_TIMEOUT).toString());
        boolean pipelining = Boolean.valueOf(map.getOrDefault(ENV_PIPELINING, DEFAULT_PIPELINING).toString());
        int pipeliningLimit = Integer.parseInt(map.getOrDefault(ENV_PIPELINING_LIMIT, DEFAULT_PIPELINING_LIMIT).toString());
        String envMessageCount = (String) map.get(ENV_MESSAGE_COUNT);
        Optional<Long> messageCount = envMessageCount != null ? Optional.of((Long.parseLong(envMessageCount))) : Optional.empty();
        String endpointPrefix = (String) map.getOrDefault(ENV_ENDPOINT_PREFIX, DEFAULT_ENDPOINT_PREFIX);
        return new HttpKafkaConsumerConfig(hostname, port, topic, groupid, pollInterval, pollTimeout, pipelining, pipeliningLimit, messageCount, endpointPrefix);
    }

    @Override
    public String toString() {
        return "HttpKafkaConsumerConfig(" +
                "hostname=" + this.hostname +
                ",port=" + this.port +
                ",topic=" + this.topic +
                ",groupid=" + this.groupid +
                ",pollInterval=" + this.pollInterval +
                ",pollTimeout=" + this.pollTimeout +
                ",pipelining=" + this.pipelining + 
                ",pipeliningLimit=" + this.pipeliningLimit +
                ",messageCount=" + (this.messageCount.isPresent() ? this.messageCount.get() : null) +
                ",endpointPrefix=" + this.endpointPrefix +
                ")";
    }
}