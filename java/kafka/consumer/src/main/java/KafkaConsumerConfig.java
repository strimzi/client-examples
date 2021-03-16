/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;
import java.util.StringTokenizer;

public class KafkaConsumerConfig {
    private static final Logger log = LogManager.getLogger(KafkaConsumerConfig.class);

    private static final long DEFAULT_MESSAGES_COUNT = 10;
    private final String bootstrapServers;
    private final String topic;
    private final String groupId;
    private final String autoOffsetReset = "earliest";
    private final String enableAutoCommit = "false";
    private final String clientRack;
    private final Long messageCount;
    private final String trustStorePassword;
    private final String trustStorePath;
    private final String keyStorePassword;
    private final String keyStorePath;
    private final String oauthClientId;
    private final String oauthClientSecret;
    private final String oauthAccessToken;
    private final String oauthRefreshToken;
    private final String oauthTokenEndpointUri;
    private final String additionalConfig;
    private final String saslLoginCallbackClass = "io.strimzi.kafka.oauth.client.JaasClientOauthLoginCallbackHandler";
    ;


    public KafkaConsumerConfig(String bootstrapServers, String topic, String groupId, String clientRack, Long messageCount,
                               String trustStorePassword, String trustStorePath, String keyStorePassword, String keyStorePath,
                               String oauthClientId, String oauthClientSecret, String oauthAccessToken, String oauthRefreshToken,
                               String oauthTokenEndpointUri, String additionalConfig) {
        this.bootstrapServers = bootstrapServers;
        this.topic = topic;
        this.groupId = groupId;
        this.clientRack = clientRack;
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
        this.additionalConfig = additionalConfig;
        ;
    }

    public static KafkaConsumerConfig fromEnv() {
        String bootstrapServers = System.getenv("BOOTSTRAP_SERVERS");
        String topic = System.getenv("TOPIC");
        String groupId = System.getenv("GROUP_ID");
        String clientRack = System.getenv("CLIENT_RACK");
        Long messageCount = System.getenv("MESSAGE_COUNT") == null ? DEFAULT_MESSAGES_COUNT : Long.valueOf(System.getenv("MESSAGE_COUNT"));
        String trustStorePassword = System.getenv("TRUSTSTORE_PASSWORD");
        String trustStorePath = System.getenv("TRUSTSTORE_PATH");
        String keyStorePassword = System.getenv("KEYSTORE_PASSWORD");
        String keyStorePath = System.getenv("KEYSTORE_PATH");
        String oauthClientId = System.getenv("OAUTH_CLIENT_ID");
        String oauthClientSecret = System.getenv("OAUTH_CLIENT_SECRET");
        String oauthAccessToken = System.getenv("OAUTH_ACCESS_TOKEN");
        String oauthRefreshToken = System.getenv("OAUTH_REFRESH_TOKEN");
        String oauthTokenEndpointUri = System.getenv("OAUTH_TOKEN_ENDPOINT_URI");
        String additionalConfig = System.getenv().getOrDefault("ADDITIONAL_CONFIG", "");

        return new KafkaConsumerConfig(bootstrapServers, topic, groupId, clientRack, messageCount, trustStorePassword, trustStorePath,
                keyStorePassword, keyStorePath, oauthClientId, oauthClientSecret, oauthAccessToken, oauthRefreshToken, oauthTokenEndpointUri,
                additionalConfig);
    }

    public static Properties createProperties(KafkaConsumerConfig config) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getGroupId());
        if (config.getClientRack() != null) {
            props.put(ConsumerConfig.CLIENT_RACK_CONFIG, config.getClientRack());
        }
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

        Properties additionalProps = new Properties();
        if (!config.getAdditionalConfig().isEmpty()) {
            StringTokenizer tok = new StringTokenizer(config.getAdditionalConfig(), System.lineSeparator());
            while (tok.hasMoreTokens()) {
                String record = tok.nextToken();
                int endIndex = record.indexOf('=');
                if (endIndex == -1) {
                    throw new RuntimeException("Failed to parse Map from String");
                }
                String key = record.substring(0, endIndex);
                String value = record.substring(endIndex + 1);
                additionalProps.put(key.trim(), value.trim());
            }
        }

        if ((config.getOauthAccessToken() != null)
                || (config.getOauthTokenEndpointUri() != null && config.getOauthClientId() != null && config.getOauthRefreshToken() != null)
                || (config.getOauthTokenEndpointUri() != null && config.getOauthClientId() != null && config.getOauthClientSecret() != null))    {
            log.info("Configuring OAuth");
            props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;");
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL".equals(props.getProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG)) ? "SASL_SSL" : "SASL_PLAINTEXT");
            props.put(SaslConfigs.SASL_MECHANISM, "OAUTHBEARER");
            if (!(additionalProps.containsKey(SaslConfigs.SASL_MECHANISM) && additionalProps.getProperty(SaslConfigs.SASL_MECHANISM).equals("PLAIN"))) {
                props.put(SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS, config.saslLoginCallbackClass);
            }
        }

        // override properties with defined additional properties
        props.putAll(additionalProps);

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

    public String getClientRack() {
        return clientRack;
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

    public String getAdditionalConfig() {
        return additionalConfig;
    }

    @Override
    public String toString() {
        return "KafkaConsumerConfig{" +
            "bootstrapServers='" + bootstrapServers + '\'' +
            ", topic='" + topic + '\'' +
            ", groupId='" + groupId + '\'' +
            ", autoOffsetReset='" + autoOffsetReset + '\'' +
            ", enableAutoCommit='" + enableAutoCommit + '\'' +
            ", clientRack='" + clientRack + '\'' +
            ", messageCount=" + messageCount +
            ", trustStorePassword='" + trustStorePassword + '\'' +
            ", trustStorePath='" + trustStorePath + '\'' +
            ", keyStorePassword='" + keyStorePassword + '\'' +
            ", keyStorePath='" + keyStorePath + '\'' +
            ", oauthClientId='" + oauthClientId + '\'' +
            ", oauthClientSecret='" + oauthClientSecret + '\'' +
            ", oauthAccessToken='" + oauthAccessToken + '\'' +
            ", oauthRefreshToken='" + oauthRefreshToken + '\'' +
            ", oauthTokenEndpointUri='" + oauthTokenEndpointUri + '\'' +
            ", additionalConfig='" + additionalConfig + '\'' +
            '}';
    }
}
