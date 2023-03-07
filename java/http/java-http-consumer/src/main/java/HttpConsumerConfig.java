/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
import io.strimzi.common.TracingSystem;

public class HttpConsumerConfig {

    private static final String DEFAULT_HOSTNAME = "localhost";
    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_TOPIC = "my-topic";
    private static final String DEFAULT_GROUPID = "my-group";
    private static final String DEFAULT_CLIENTID = "my-consumer";

    private static final long DEFAULT_MESSAGES_COUNT = 10;
    private static final int DEFAULT_POLL_INTERVAL = 1000;
    private static final int DEFAULT_POLL_TIMEOUT = 100;

    private final String hostName;
    private final int port;
    private final String topic;
    private final String groupId;
    private final String clientId;
    private final long messageCount;
    private final int pollInterval;
    private final int pollTimeout;
    private final TracingSystem tracingSystem;

    private HttpConsumerConfig(String hostName, int port, String topic, String groupId, String clientId,
                               long messageCount, int pollInterval, int pollTimeout, TracingSystem tracingSystem) {
        this.hostName = hostName;
        this.port = port;
        this.topic = topic;
        this.groupId = groupId;
        this.clientId = clientId;
        this.messageCount = messageCount;
        this.pollInterval = pollInterval;
        this.pollTimeout = pollTimeout;
        this.tracingSystem = tracingSystem;
    }

    public static HttpConsumerConfig fromEnv() {
        String hostName = System.getenv("STRIMZI_HOSTNAME") == null ? DEFAULT_HOSTNAME : System.getenv("STRIMZI_HOSTNAME");
        int port = System.getenv("STRIMZI_PORT") == null ? DEFAULT_PORT : Integer.parseInt(System.getenv("STRIMZI_PORT"));
        String topic = System.getenv("STRIMZI_TOPIC") == null ? DEFAULT_TOPIC : System.getenv("STRIMZI_TOPIC");
        String groupId = System.getenv("STRIMZI_GROUP_ID") == null ? DEFAULT_GROUPID : System.getenv("STRIMZI_GROUP_ID");
        String clientId = System.getenv("STRIMZI_CLIENT_ID") == null ? DEFAULT_CLIENTID : System.getenv("STRIMZI_CLIENT_ID");
        Long messageCount = System.getenv("STRIMZI_MESSAGE_COUNT") == null ? DEFAULT_MESSAGES_COUNT : Long.parseLong(System.getenv("STRIMZI_MESSAGE_COUNT"));
        int pollInterval = System.getenv("STRIMZI_POLL_INTERVAL") == null ? DEFAULT_POLL_INTERVAL : Integer.parseInt(System.getenv("STRIMZI_POLL_INTERVAL"));
        int pollTimeout = System.getenv("STRIMZI_POLL_TIMEOUT") == null ? DEFAULT_POLL_TIMEOUT : Integer.parseInt(System.getenv("STRIMZI_POLL_TIMEOUT"));
        TracingSystem tracingSystem = TracingSystem.forValue(System.getenv().getOrDefault("STRIMZI_TRACING_SYSTEM", ""));
        return new HttpConsumerConfig(hostName, port, topic, groupId, clientId, messageCount, pollInterval, pollTimeout, tracingSystem);
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

    public long getMessageCount() {
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
                "}";
    }
}
