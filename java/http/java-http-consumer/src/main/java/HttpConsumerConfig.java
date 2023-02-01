/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
public class HttpConsumerConfig {

    private static final String DEFAULT_HOSTNAME = "localhost";
    private static final int DEFAULT_PORT = 8080;
    private static final long DEFAULT_MESSAGES_COUNT = 10;
    private static final int DEFAULT_POLL_INTERVAL = 1000;
    private static final int DEFAULT_POLL_TIMEOUT = 100;

    private final String hostName;
    private final int port;
    private final String topic;
    private final String groupId;
    private final long messageCount;
    private final int pollInterval;
    private final int pollTimeout;

    private HttpConsumerConfig(String hostName, int port, String topic, String groupId,
                               long messageCount, int pollInterval, int pollTimeout) {
        this.hostName = hostName;
        this.port = port;
        this.topic = topic;
        this.groupId = groupId;
        this.messageCount = messageCount;
        this.pollInterval = pollInterval;
        this.pollTimeout = pollTimeout;
    }

    public static HttpConsumerConfig fromEnv() {
        String hostName = System.getenv("STRIMZI_HOSTNAME") == null ? DEFAULT_HOSTNAME : System.getenv("STRIMZI_HOSTNAME");
        int port = System.getenv("STRIMZI_PORT") == null ? DEFAULT_PORT : Integer.parseInt(System.getenv("STRIMZI_PORT"));
        String topic = System.getenv("STRIMZI_TOPIC");
        String groupId = System.getenv("STRIMZI_GROUP_ID");
        Long messageCount = System.getenv("STRIMZI_MESSAGE_COUNT") == null ? DEFAULT_MESSAGES_COUNT : Long.parseLong(System.getenv("STRIMZI_MESSAGE_COUNT"));
        int pollInterval = System.getenv("STRIMZI_POLL_INTERVAL") == null ? DEFAULT_POLL_INTERVAL : Integer.parseInt(System.getenv("STRIMZI_POLL_INTERVAL"));
        int pollTimeout = System.getenv("STRIMZI_POLL_TIMEOUT") == null ? DEFAULT_POLL_TIMEOUT : Integer.parseInt(System.getenv("STRIMZI_POLL_TIMEOUT"));
        return new HttpConsumerConfig(hostName, port, topic, groupId, messageCount, pollInterval, pollTimeout);
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

    public long getMessageCount() {
        return messageCount;
    }

    public int getPollInterval() {
        return pollInterval;
    }

    public int getPollTimeout() {
        return pollTimeout;
    }

    @Override
    public String toString() {
        return "HttpConsumerConfig{" +
                "hostName='" + hostName + "'" +
                ", port=" + port +
                ", topic=" + topic +
                ", groupId=" + groupId +
                ", messageCount=" + messageCount +
                ", pollInterval=" + pollInterval +
                ", pollTimeout=" + pollTimeout +
                "}";
    }
}
