public class KafkaProducerConfig {
    private final String bootstrapServers;
    private final String topic;
    private final int timer;
    private final int numberOfKeys;
    private final Long messageCount;
    private String acks = "1";
    private final String trustStorePassword;
    private final String trustStorePath;
    private final String keyStorePassword;
    private final String keyStorePath;
    private static final long DEFAULT_MESSAGES_COUNT = 10;

    public KafkaProducerConfig(String bootstrapServers, String topic, int timer, int numberOfKeys, Long messageCount, String trustStorePassword, String trustStorePath, String keyStorePassword, String keyStorePath) {
        this.bootstrapServers = bootstrapServers;
        this.topic = topic;
        this.timer = timer;
        this.numberOfKeys = numberOfKeys;
        this.messageCount = messageCount;
        this.trustStorePassword = trustStorePassword;
        this.trustStorePath = trustStorePath;
        this.keyStorePassword = keyStorePassword;
        this.keyStorePath = keyStorePath;
    }

    public static KafkaProducerConfig fromEnv() {
        String bootstrapServers = System.getenv("BOOTSTRAP_SERVERS");
        String topic = System.getenv("TOPIC");
        int timer = Integer.valueOf(System.getenv("TIMER"));
        int numberOfKeys = Integer.parseInt(System.getenv("MESSAGE_KEY_COUNT") == null ? "1" : System.getenv("MESSAGE_KEY_COUNT"));
        Long messageCount = System.getenv("MESSAGE_COUNT") == null ? DEFAULT_MESSAGES_COUNT : Long.valueOf(System.getenv("MESSAGE_COUNT"));
        String trustStorePassword = System.getenv("TRUSTSTORE_PASSWORD") == null ? null : System.getenv("TRUSTSTORE_PASSWORD");
        String trustStorePath = System.getenv("TRUSTSTORE_PATH") == null ? null : System.getenv("TRUSTSTORE_PATH");
        String keyStorePassword = System.getenv("KEYSTORE_PASSWORD") == null ? null : System.getenv("KEYSTORE_PASSWORD");
        String keyStorePath = System.getenv("KEYSTORE_PATH") == null ? null : System.getenv("KEYSTORE_PATH");

        return new KafkaProducerConfig(bootstrapServers, topic, timer, numberOfKeys, messageCount, trustStorePassword, trustStorePath, keyStorePassword, keyStorePath);
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public String getTopic() {
        return topic;
    }

    public int getTimer() {
        return timer;
    }

    public int getNumberOfKeys() {
        return numberOfKeys;
    }

    public Long getMessageCount() {
        return messageCount;
    }

    public String getAcks() {
        return acks;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public String getTrustStorePath() {
        return trustStorePath;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public String getKeyStorePath() {
        return keyStorePath;
    }
}
