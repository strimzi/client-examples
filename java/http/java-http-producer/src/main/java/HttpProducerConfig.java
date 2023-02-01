/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
public class HttpProducerConfig {

    private static final String DEFAULT_HOSTNAME = "localhost";
    private static final int DEFAULT_PORT = 8080;
    private static final long DEFAULT_MESSAGES_COUNT = 10;
    private static final String DEFAULT_MESSAGE = "Hello world";

    private final String hostName;
    private final int port;
    private final String topic;
    private final long messageCount;
    private final int delay;
    private final String message;
    private final String headers;


    private HttpProducerConfig(String hostName, int port, String topic, Long messageCount,
                               int delay, String message, String headers) {
        this.hostName = hostName;
        this.port = port;
        this.topic = topic;
        this.messageCount = messageCount;
        this.delay = delay;
        this.message = message;
        this.headers = headers;
    }

    public static HttpProducerConfig fromEnv() {
        String hostName = System.getenv("STRIMZI_HOSTNAME") == null ? DEFAULT_HOSTNAME : System.getenv("STRIMZI_HOSTNAME");
        int port = System.getenv("STRIMZI_PORT") == null ? DEFAULT_PORT : Integer.parseInt(System.getenv("STRIMZI_PORT"));
        String topic = System.getenv("STRIMZI_TOPIC");
        Long messageCount = System.getenv("STRIMZI_MESSAGE_COUNT") == null ? DEFAULT_MESSAGES_COUNT : Long.parseLong(System.getenv("STRIMZI_MESSAGE_COUNT"));
        int delay = Integer.parseInt(System.getenv("STRIMZI_DELAY_MS"));
        String message = System.getenv("STRIMZI_MESSAGE") == null ? DEFAULT_MESSAGE : System.getenv("STRIMZI_MESSAGE");
        String headers = System.getenv("STRIMZI_HEADERS");
        return new HttpProducerConfig(hostName, port, topic, messageCount, delay, message, headers);
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

    public long getMessageCount() {
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

    @Override
    public String toString() {
        return "HttpProducerConfig{" +
                "hostName='" + hostName + "'" +
                ", port=" + port +
                ", topic=" + topic +
                ", messageCount=" + messageCount +
                ", delay=" + delay +
                ", message=" + message +
                ", headers=" + headers +
                "}";
    }
}
