/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;
import java.util.StringTokenizer;

public class KafkaStreamsConfig {
    private static final Logger log = LogManager.getLogger(KafkaStreamsConfig.class);

    private static final int DEFAULT_COMMIT_INTERVAL_MS = 5000;
    private final String bootstrapServers;
    private final String applicationId;
    private final String sourceTopic;
    private final String targetTopic;
    private final int commitIntervalMs;
    private final String sslTruststoreCertificates;
    private final String sslKeystoreKey;
    private final String sslKeystoreCertificateChain;
    private final String oauthClientId;
    private final String oauthClientSecret;
    private final String oauthAccessToken;
    private final String oauthRefreshToken;
    private final String oauthTokenEndpointUri;
    private final String additionalConfig;
    private final String saslLoginCallbackClass = "io.strimzi.kafka.oauth.client.JaasClientOauthLoginCallbackHandler";


    public KafkaStreamsConfig(String bootstrapServers, String applicationId, String sourceTopic, String targetTopic,
                              int commitIntervalMs, String sslTruststoreCertificates, String sslKeystoreKey, String sslKeystoreCertificateChain,
                              String oauthClientId, String oauthClientSecret, String oauthAccessToken, String oauthRefreshToken,
                              String oauthTokenEndpointUri, String additionalConfig) {
        this.bootstrapServers = bootstrapServers;
        this.applicationId = applicationId;
        this.sourceTopic = sourceTopic;
        this.targetTopic = targetTopic;
        this.commitIntervalMs = commitIntervalMs;
        this.sslTruststoreCertificates = sslTruststoreCertificates;
        this.sslKeystoreKey = sslKeystoreKey;
        this.sslKeystoreCertificateChain = sslKeystoreCertificateChain;
        this.oauthClientId = oauthClientId;
        this.oauthClientSecret = oauthClientSecret;
        this.oauthAccessToken = oauthAccessToken;
        this.oauthRefreshToken = oauthRefreshToken;
        this.oauthTokenEndpointUri = oauthTokenEndpointUri;
        this.additionalConfig = additionalConfig;
    }

    public static KafkaStreamsConfig fromEnv() {
        String bootstrapServers = System.getenv("BOOTSTRAP_SERVERS");
        String sourceTopic = System.getenv("SOURCE_TOPIC");
        String targetTopic = System.getenv("TARGET_TOPIC");
        String applicationId = System.getenv("APPLICATION_ID");
        int commitIntervalMs = System.getenv("COMMIT_INTERVAL_MS") == null ? DEFAULT_COMMIT_INTERVAL_MS : Integer.parseInt(System.getenv("COMMIT_INTERVAL_MS"));
        String sslTruststoreCertificates = System.getenv("CA_CRT");
        String sslKeystoreKey = System.getenv("USER_KEY");
        String sslKeystoreCertificateChain = System.getenv("USER_CRT");
        String oauthClientId = System.getenv("OAUTH_CLIENT_ID");
        String oauthClientSecret = System.getenv("OAUTH_CLIENT_SECRET");
        String oauthAccessToken = System.getenv("OAUTH_ACCESS_TOKEN");
        String oauthRefreshToken = System.getenv("OAUTH_REFRESH_TOKEN");
        String oauthTokenEndpointUri = System.getenv("OAUTH_TOKEN_ENDPOINT_URI");
        String additionalConfig = System.getenv().getOrDefault("ADDITIONAL_CONFIG", "");

        return new KafkaStreamsConfig(bootstrapServers, applicationId, sourceTopic, targetTopic, commitIntervalMs,
                sslTruststoreCertificates, sslKeystoreKey, sslKeystoreCertificateChain, oauthClientId, oauthClientSecret,
                oauthAccessToken, oauthRefreshToken, oauthTokenEndpointUri, additionalConfig);
    }

    public static Properties createProperties(KafkaStreamsConfig config) {
        Properties props = new Properties();

        props.put(StreamsConfig.APPLICATION_ID_CONFIG, config.getApplicationId());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, config.getCommitInterval());
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        if (config.getSslTruststoreCertificates() != null)   {
            log.info("Configuring truststore");
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
            props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "PEM");
            props.put(SslConfigs.SSL_TRUSTSTORE_CERTIFICATES_CONFIG, config.getSslTruststoreCertificates());
        }

        if (config.getSslKeystoreCertificateChain() != null && config.getSslKeystoreKey() != null)   {
            log.info("Configuring keystore");
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
            props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PEM");
            props.put(SslConfigs.SSL_KEYSTORE_CERTIFICATE_CHAIN_CONFIG, config.getSslKeystoreCertificateChain());
            props.put(SslConfigs.SSL_KEYSTORE_KEY_CONFIG, config.getSslKeystoreKey());
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

    public String getApplicationId() {
        return applicationId;
    }

    public String getSourceTopic() {
        return sourceTopic;
    }

    public String getTargetTopic() {
        return targetTopic;
    }

    public int getCommitInterval() {
        return commitIntervalMs;
    }

    public String getSslTruststoreCertificates() {
        return sslTruststoreCertificates;
    }

    public String getSslKeystoreKey() {
        return sslKeystoreKey;
    }

    public String getSslKeystoreCertificateChain() {
        return sslKeystoreCertificateChain;
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
        return "KafkaStreamsConfig{" +
            "bootstrapServers='" + bootstrapServers + '\'' +
            ", applicationId='" + applicationId + '\'' +
            ", sourceTopic='" + sourceTopic + '\'' +
            ", targetTopic='" + targetTopic + '\'' +
            ", commitIntervalMs=" + commitIntervalMs +
            ", trustStorePassword='" + sslTruststoreCertificates + '\'' +
            ", trustStorePath='" + sslKeystoreKey + '\'' +
            ", keyStorePassword='" + sslKeystoreCertificateChain + '\'' +
            ", oauthClientId='" + oauthClientId + '\'' +
            ", oauthClientSecret='" + oauthClientSecret + '\'' +
            ", oauthAccessToken='" + oauthAccessToken + '\'' +
            ", oauthRefreshToken='" + oauthRefreshToken + '\'' +
            ", oauthTokenEndpointUri='" + oauthTokenEndpointUri + '\'' +
            ", additionalConfig='" + additionalConfig + '\'' +
            '}';
    }
}
