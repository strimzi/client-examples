/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
import io.strimzi.common.ConfigUtil;
import io.strimzi.common.TracingSystem;

import java.util.Properties;

public class HttpConsumerConfig {

    private static final String DEFAULT_HOSTNAME = "localhost";
    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_TOPIC = "my-topic";
    private static final String DEFAULT_GROUPID = "my-group";
    private static final String DEFAULT_CLIENTID = "my-consumer";
    private static final int DEFAULT_POLL_INTERVAL = 1000;
    private static final int DEFAULT_POLL_TIMEOUT = 100;
    private static final String KAFKA_PREFIX = "KAFKA_";
    private static final String ENABLE_AUTO_COMMIT_CONFIG = "enable.auto.commit";

    private final String hostName;
    private final int port;
    private final String topic;
    private final String groupId;
    private final String clientId;
    private final Long messageCount;
    private final int pollInterval;
    private final int pollTimeout;
    private final TracingSystem tracingSystem;
    private final Properties properties;

    @SuppressWarnings("checkstyle:ParameterNumber")
    private HttpConsumerConfig(String hostName, int port, String topic, String groupId, String clientId,
                               Long messageCount, int pollInterval, int pollTimeout, TracingSystem tracingSystem, Properties properties) {
        this.hostName = hostName;
        this.port = port;
        this.topic = topic;
        this.groupId = groupId;
        this.clientId = clientId;
        this.messageCount = messageCount;
        this.pollInterval = pollInterval;
        this.pollTimeout = pollTimeout;
        this.tracingSystem = tracingSystem;
        this.properties = properties;

    }

    @SuppressWarnings("checkstyle:NPathComplexity")
    public static HttpConsumerConfig fromEnv() {
        String hostName = System.getenv("STRIMZI_HOSTNAME") == null ? DEFAULT_HOSTNAME : System.getenv("STRIMZI_HOSTNAME");
        int port = System.getenv("STRIMZI_PORT") == null ? DEFAULT_PORT : Integer.parseInt(System.getenv("STRIMZI_PORT"));
        String topic = System.getenv("STRIMZI_TOPIC") == null ? DEFAULT_TOPIC : System.getenv("STRIMZI_TOPIC");
        String groupId = System.getenv("STRIMZI_GROUP_ID") == null ? DEFAULT_GROUPID : System.getenv("STRIMZI_GROUP_ID");
        String clientId = System.getenv("STRIMZI_CLIENT_ID") == null ? DEFAULT_CLIENTID : System.getenv("STRIMZI_CLIENT_ID");
        Long messageCount = System.getenv("STRIMZI_MESSAGE_COUNT") == null ? null : Long.parseLong(System.getenv("STRIMZI_MESSAGE_COUNT"));
        int pollInterval = System.getenv("STRIMZI_POLL_INTERVAL") == null ? DEFAULT_POLL_INTERVAL : Integer.parseInt(System.getenv("STRIMZI_POLL_INTERVAL"));
        int pollTimeout = System.getenv("STRIMZI_POLL_TIMEOUT") == null ? DEFAULT_POLL_TIMEOUT : Integer.parseInt(System.getenv("STRIMZI_POLL_TIMEOUT"));
        TracingSystem tracingSystem = TracingSystem.forValue(System.getenv().getOrDefault("STRIMZI_TRACING_SYSTEM", ""));
        Properties properties = ConfigUtil.getKafkaPropertiesFromEnv();
        return new HttpConsumerConfig(hostName, port, topic, groupId, clientId, messageCount, pollInterval, pollTimeout, tracingSystem, properties);
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    public String getTopic() {
        return topic;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getClientId() {
        return clientId;
    }

    public Long getMessageCount() {
        return messageCount;
    }

    public int getPollInterval() {
        return pollInterval;
    }

    public int getPollTimeout() {
        return pollTimeout;
    }

    public TracingSystem getTracingSystem() {
        return tracingSystem;
    }

    public boolean getEnableAutoCommit() {
        return Boolean.parseBoolean(properties.getProperty(ENABLE_AUTO_COMMIT_CONFIG));
    }


    public Properties getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return "HttpConsumerConfig{" +
                "hostName='" + hostName + "'" +
                ", port=" + port +
                ", topic=" + topic +
                ", groupId=" + groupId +
                ", clientId=" + clientId +
                ", messageCount=" + messageCount +
                ", pollInterval=" + pollInterval +
                ", pollTimeout=" + pollTimeout +
                ", tracingSystem=" + tracingSystem +
                ", properties ='" + properties + '\'' +
                "}";
    }
}
