/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;
import java.util.StringTokenizer;

public class KafkaProducerConfig {
    private static final Logger log = LogManager.getLogger(KafkaProducerConfig.class);

    private static final long DEFAULT_MESSAGES_COUNT = 10;
    private static final String DEFAULT_MESSAGE = "Hello world";
    private final String bootstrapServers;
    private final String topic;
    private final int delay;
    private final Long messageCount;
    private final String message;
    private final String acks;
    private final String headers;
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
    private final TracingSystem tracingSystem;

    public KafkaProducerConfig(String bootstrapServers, String topic, int delay, Long messageCount, String message,
                               String sslTruststoreCertificates, String sslKeystoreKey, String sslKeystoreCertificateChain,
                               String oauthClientId, String oauthClientSecret, String oauthAccessToken, String oauthRefreshToken,
                               String oauthTokenEndpointUri, String acks, String additionalConfig, String headers, TracingSystem tracingSystem) {
        this.bootstrapServers = bootstrapServers;
        this.topic = topic;
        this.delay = delay;
        this.messageCount = messageCount;
        this.message = message;
        this.sslTruststoreCertificates = sslTruststoreCertificates;
        this.sslKeystoreKey = sslKeystoreKey;
        this.sslKeystoreCertificateChain = sslKeystoreCertificateChain;
        this.oauthClientId = oauthClientId;
        this.oauthClientSecret = oauthClientSecret;
        this.oauthAccessToken = oauthAccessToken;
        this.oauthRefreshToken = oauthRefreshToken;
        this.oauthTokenEndpointUri = oauthTokenEndpointUri;
        this.acks = acks;
        this.headers = headers;
        this.additionalConfig = additionalConfig;
        this.tracingSystem = tracingSystem;
    }

    public static KafkaProducerConfig fromEnv() {
        String bootstrapServers = System.getenv("BOOTSTRAP_SERVERS");
        String topic = System.getenv("TOPIC");
        int delay = Integer.parseInt(System.getenv("DELAY_MS"));
        Long messageCount = System.getenv("MESSAGE_COUNT") == null ? DEFAULT_MESSAGES_COUNT : Long.parseLong(System.getenv("MESSAGE_COUNT"));
        String message = System.getenv("MESSAGE") == null ? DEFAULT_MESSAGE : System.getenv("MESSAGE");
        String sslTruststoreCertificates = System.getenv("CA_CRT");
        String sslKeystoreKey = System.getenv("USER_KEY");
        String sslKeystoreCertificateChain = System.getenv("USER_CRT");
        String oauthClientId = System.getenv("OAUTH_CLIENT_ID");
        String oauthClientSecret = System.getenv("OAUTH_CLIENT_SECRET");
        String oauthAccessToken = System.getenv("OAUTH_ACCESS_TOKEN");
        String oauthRefreshToken = System.getenv("OAUTH_REFRESH_TOKEN");
        String oauthTokenEndpointUri = System.getenv("OAUTH_TOKEN_ENDPOINT_URI");
        String acks = System.getenv().getOrDefault("PRODUCER_ACKS", "1");
        String headers = System.getenv("HEADERS");
        String additionalConfig = System.getenv().getOrDefault("ADDITIONAL_CONFIG", "");
        TracingSystem tracingSystem = TracingSystem.forValue(System.getenv().getOrDefault("TRACING_SYSTEM", ""));

        return new KafkaProducerConfig(bootstrapServers, topic, delay, messageCount, message, sslTruststoreCertificates,
                sslKeystoreKey, sslKeystoreCertificateChain, oauthClientId, oauthClientSecret, oauthAccessToken, oauthRefreshToken,
                oauthTokenEndpointUri, acks, additionalConfig, headers, tracingSystem);
    }

    public static Properties createProperties(KafkaProducerConfig config) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        props.put(ProducerConfig.ACKS_CONFIG, config.getAcks());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

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

    public String getTopic() {
        return topic;
    }

    public int getDelay() {
        return delay;
    }

    public Long getMessageCount() {
        return messageCount;
    }

    public String getMessage() {
        return message;
    }

    public String getAcks() {
        return acks;
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

    public String getHeaders() {
        return headers;
    }

    public String getAdditionalConfig() {
        return additionalConfig;
    }

    public TracingSystem getTracingSystem() {
        return tracingSystem;
    }

    @Override
    public String toString() {
        return "KafkaProducerConfig{" +
            "bootstrapServers='" + bootstrapServers + '\'' +
            ", topic='" + topic + '\'' +
            ", delay=" + delay +
            ", messageCount=" + messageCount +
            ", message='" + message + '\'' +
            ", acks='" + acks + '\'' +
            ", headers='" + headers + '\'' +
            ", sslTruststoreCertificates='" + sslTruststoreCertificates + '\'' +
            ", sslKeystoreKey='" + sslKeystoreKey + '\'' +
            ", sslKeystoreCertificateChain='" + sslKeystoreCertificateChain + '\'' +
            ", oauthClientId='" + oauthClientId + '\'' +
            ", oauthClientSecret='" + oauthClientSecret + '\'' +
            ", oauthAccessToken='" + oauthAccessToken + '\'' +
            ", oauthRefreshToken='" + oauthRefreshToken + '\'' +
            ", oauthTokenEndpointUri='" + oauthTokenEndpointUri + '\'' +
            ", additionalConfig='" + additionalConfig + '\'' +
            ", tracingSystem='" + tracingSystem + '\'' +
            '}';
    }
}
