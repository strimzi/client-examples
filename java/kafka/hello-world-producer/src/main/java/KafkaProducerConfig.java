import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

public class KafkaProducerConfig {
    private static final Logger log = LogManager.getLogger(KafkaProducerConfig.class);

    private static final long DEFAULT_MESSAGES_COUNT = 10;
    private final String bootstrapServers;
    private final String topic;
    private final int delay;
    private final Long messageCount;
    private String acks = "1";
    private final String trustStorePassword;
    private final String trustStorePath;
    private final String keyStorePassword;
    private final String keyStorePath;
    private final String oauthClientId;
    private final String oauthClientSecret;
    private final String oauthAccessToken;
    private final String oauthRefreshToken;
    private final String oauthTokenEndpointUri;

    public KafkaProducerConfig(String bootstrapServers, String topic, int delay, Long messageCount, String trustStorePassword, String trustStorePath, String keyStorePassword, String keyStorePath, String oauthClientId, String oauthClientSecret, String oauthAccessToken, String oauthRefreshToken, String oauthTokenEndpointUri) {
        this.bootstrapServers = bootstrapServers;
        this.topic = topic;
        this.delay = delay;
        this.messageCount = messageCount;
        this.trustStorePassword = trustStorePassword;
        this.trustStorePath = trustStorePath;
        this.keyStorePassword = keyStorePassword;
        this.keyStorePath = keyStorePath;
        this.oauthClientId = oauthClientId;
        this.oauthClientSecret = oauthClientSecret;
        this.oauthAccessToken = oauthAccessToken;
        this.oauthRefreshToken = oauthRefreshToken;
        this.oauthTokenEndpointUri = oauthTokenEndpointUri;
    }

    public static KafkaProducerConfig fromEnv() {
        String bootstrapServers = System.getenv("BOOTSTRAP_SERVERS");
        String topic = System.getenv("TOPIC");
        int delay = Integer.valueOf(System.getenv("DELAY_MS"));
        Long messageCount = System.getenv("MESSAGE_COUNT") == null ? DEFAULT_MESSAGES_COUNT : Long.valueOf(System.getenv("MESSAGE_COUNT"));
        String trustStorePassword = System.getenv("TRUSTSTORE_PASSWORD") == null ? null : System.getenv("TRUSTSTORE_PASSWORD");
        String trustStorePath = System.getenv("TRUSTSTORE_PATH") == null ? null : System.getenv("TRUSTSTORE_PATH");
        String keyStorePassword = System.getenv("KEYSTORE_PASSWORD") == null ? null : System.getenv("KEYSTORE_PASSWORD");
        String keyStorePath = System.getenv("KEYSTORE_PATH") == null ? null : System.getenv("KEYSTORE_PATH");
        String oauthClientId = System.getenv("OAUTH_CLIENT_ID");
        String oauthClientSecret = System.getenv("OAUTH_CLIENT_SECRET");
        String oauthAccessToken = System.getenv("OAUTH_ACCESS_TOKEN");
        String oauthRefreshToken = System.getenv("OAUTH_REFRESH_TOKEN");
        String oauthTokenEndpointUri = System.getenv("OAUTH_TOKEN_ENDPOINT_URI");

        return new KafkaProducerConfig(bootstrapServers, topic, delay, messageCount, trustStorePassword, trustStorePath, keyStorePassword, keyStorePath, oauthClientId, oauthClientSecret, oauthAccessToken, oauthRefreshToken, oauthTokenEndpointUri);
    }

    public static Properties createProperties(KafkaProducerConfig config) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        props.put(ProducerConfig.ACKS_CONFIG, config.getAcks());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

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

        if ((config.getOauthAccessToken() != null)
                || (config.getOauthTokenEndpointUri() != null && config.getOauthClientId() != null && config.getOauthRefreshToken() != null)
                || (config.getOauthTokenEndpointUri() != null && config.getOauthClientId() != null && config.getOauthClientSecret() != null))    {
            props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;");
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL".equals(props.getProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG)) ? "SASL_SSL" : "SASL_PLAINTEXT");
            props.put(SaslConfigs.SASL_MECHANISM, "OAUTHBEARER");
            props.put(SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS, "io.strimzi.kafka.oauth.client.JaasClientOauthLoginCallbackHandler");
        }

        return props;
    }
    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public String getTopic() {
        return topic;
    }

    public int getDelay() {
        return delay;
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

    public String getOauthClientId() {
        return oauthClientId;
    }

    public String getOauthClientSecret() {
        return oauthClientSecret;
    }

    public String getOauthAccessToken() {
        return oauthAccessToken;
    }

    public String getOauthRefreshToken() {
        return oauthRefreshToken;
    }

    public String getOauthTokenEndpointUri() {
        return oauthTokenEndpointUri;
    }
}
