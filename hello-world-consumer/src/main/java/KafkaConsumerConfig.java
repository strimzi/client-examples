import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

public class KafkaConsumerConfig {
    private static final Logger log = LogManager.getLogger(KafkaConsumerConfig.class);

    private static final long DEFAULT_MESSAGES_COUNT = 10;
    private final String bootstrapServers;
    private final String topic;
    private final String groupId;
    private final String autoOffsetReset = "earliest";
    private final String enableAutoCommit = "false";
    private final Long messageCount;
    private final String trustStorePassword;
    private final String trustStorePath;
    private final String keyStorePassword;
    private final String keyStorePath;

    public KafkaConsumerConfig(String bootstrapServers, String topic, String groupId, Long messageCount, String trustStorePassword, String trustStorePath, String keyStorePassword, String keyStorePath) {
        this.bootstrapServers = bootstrapServers;
        this.topic = topic;
        this.groupId = groupId;
        this.messageCount = messageCount;
        this.trustStorePassword = trustStorePassword;
        this.trustStorePath = trustStorePath;
        this.keyStorePassword = keyStorePassword;
        this.keyStorePath = keyStorePath;
    }

    public static KafkaConsumerConfig fromEnv() {
        String bootstrapServers = System.getenv("BOOTSTRAP_SERVERS");
        String topic = System.getenv("TOPIC");
        String groupId = System.getenv("GROUP_ID");
        Long messageCount = System.getenv("MESSAGE_COUNT") == null ? DEFAULT_MESSAGES_COUNT : Long.valueOf(System.getenv("MESSAGE_COUNT"));
        String trustStorePassword = System.getenv("TRUSTSTORE_PASSWORD") == null ? null : System.getenv("TRUSTSTORE_PASSWORD");
        String trustStorePath = System.getenv("TRUSTSTORE_PATH") == null ? null : System.getenv("TRUSTSTORE_PATH");
        String keyStorePassword = System.getenv("KEYSTORE_PASSWORD") == null ? null : System.getenv("KEYSTORE_PASSWORD");
        String keyStorePath = System.getenv("KEYSTORE_PATH") == null ? null : System.getenv("KEYSTORE_PATH");

        return new KafkaConsumerConfig(bootstrapServers, topic, groupId, messageCount, trustStorePassword, trustStorePath, keyStorePassword, keyStorePath);
    }

    public static Properties createProperties(KafkaConsumerConfig config) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getGroupId());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.getAutoOffsetReset());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, config.getEnableAutoCommit());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        if (config.getTrustStorePassword() != null && config.getTrustStorePath() != null)   {
            log.info("Configuring truststore");
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
            props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "PKCS12");
            props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, config.getTrustStorePassword());
            props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, config.getTrustStorePath());
        }

        if (config.getKeyStorePassword() != null && config.getKeyStorePath() != null)   {
            log.info("Configuring keystore");
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
            props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PKCS12");
            props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, config.getKeyStorePassword());
            props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, config.getKeyStorePath());
        }
        ;
        return props;
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public String getTopic() {
        return topic;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getAutoOffsetReset() {
        return autoOffsetReset;
    }

    public String getEnableAutoCommit() {
        return enableAutoCommit;
    }

    public Long getMessageCount() {
        return messageCount;
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
