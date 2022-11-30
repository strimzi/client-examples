/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;

import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import static java.util.Map.entry;
import static org.apache.kafka.common.requests.DeleteAclsResponse.log;

public class KafkaConsumerConfig {
    private static final long DEFAULT_MESSAGES_COUNT = 10;
    private final String topic;
    private final String autoOffsetReset = "earliest";
    private final String enableAutoCommit = "false";
    private final Long messageCount;
    private final String oauthClientId;
    private final String oauthClientSecret;
    private final String oauthAccessToken;
    private final String oauthRefreshToken;
    private final String oauthTokenEndpointUri;
    private final String saslLoginCallbackClass = "io.strimzi.kafka.oauth.client.JaasClientOauthLoginCallbackHandler";
    private final TracingSystem tracingSystem;

    private static final Map<String, String> DEFAULT_PROPERTIES = Map.ofEntries(
            entry(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer"),
            entry(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer"));

    private static final Map<String, String> DEFAULT_TRUSTSTORE_CONFIGS = Map.ofEntries(
            entry(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL"),
            entry(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "PEM"));

    private static final Map<String, String> DEFAULT_KEYSTORE_CONFIGS = Map.ofEntries(
            entry(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL"),
            entry(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PEM"));

    public KafkaConsumerConfig(String topic, Long messageCount, String oauthClientId,
                               String oauthClientSecret, String oauthAccessToken, String oauthRefreshToken,
                               String oauthTokenEndpointUri, TracingSystem tracingSystem) {
        this.topic = topic;
        this.messageCount = messageCount;
        this.oauthClientId = oauthClientId;
        this.oauthClientSecret = oauthClientSecret;
        this.oauthAccessToken = oauthAccessToken;
        this.oauthRefreshToken = oauthRefreshToken;
        this.oauthTokenEndpointUri = oauthTokenEndpointUri;
        this.tracingSystem = tracingSystem;
    }

    public static KafkaConsumerConfig fromEnv() {
        String topic = System.getenv("TOPIC");
        Long messageCount = System.getenv("MESSAGE_COUNT") == null ? DEFAULT_MESSAGES_COUNT : Long.parseLong(System.getenv("MESSAGE_COUNT"));
        String oauthClientId = System.getenv("OAUTH_CLIENT_ID");
        String oauthClientSecret = System.getenv("OAUTH_CLIENT_SECRET");
        String oauthAccessToken = System.getenv("OAUTH_ACCESS_TOKEN");
        String oauthRefreshToken = System.getenv("OAUTH_REFRESH_TOKEN");
        String oauthTokenEndpointUri = System.getenv("OAUTH_TOKEN_ENDPOINT_URI");
        TracingSystem tracingSystem = TracingSystem.forValue(System.getenv().getOrDefault("TRACING_SYSTEM", ""));

        return new KafkaConsumerConfig(topic, messageCount, oauthClientId, oauthClientSecret,
                oauthAccessToken, oauthRefreshToken, oauthTokenEndpointUri,tracingSystem);
    }

    public static String convertEnvVarToPropertyKey(String envVar) {
        System.out.println("ENV_VAR is " + envVar);
        return envVar.substring(6).toLowerCase().replace("_", ".");
    }

    public static Properties createProperties(KafkaConsumerConfig config) {

        Map<String, String> userConfigs = System.getenv()
                .entrySet()
                .stream()
                .filter(map -> map.getKey().startsWith("KAFKA_"))
                .collect(Collectors.toMap(map -> convertEnvVarToPropertyKey(map.getKey()), map -> map.getValue()));

        Properties props = new Properties();
        props.putAll(DEFAULT_PROPERTIES);

        if (userConfigs.containsKey(SslConfigs.SSL_TRUSTSTORE_CERTIFICATES_CONFIG)) {
            props.putAll(DEFAULT_TRUSTSTORE_CONFIGS);
        }

        if (userConfigs.containsKey(SslConfigs.SSL_KEYSTORE_CERTIFICATE_CHAIN_CONFIG)
                && userConfigs.containsKey(SslConfigs.SSL_KEYSTORE_KEY_CONFIG)) {
            props.putAll(DEFAULT_KEYSTORE_CONFIGS);
        }

      /*  if ((config.getOauthAccessToken() != null)
                || (config.getOauthTokenEndpointUri() != null && config.getOauthClientId() != null && config.getOauthRefreshToken() != null)
                || (config.getOauthTokenEndpointUri() != null && config.getOauthClientId() != null && config.getOauthClientSecret() != null))    {
            log.info("Configuring OAuth");
            props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;");
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL".equals(props.getProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG)) ? "SASL_SSL" : "SASL_PLAINTEXT");
            props.put(SaslConfigs.SASL_MECHANISM, "OAUTHBEARER");
            if (!(userConfigs.containsKey(SaslConfigs.SASL_MECHANISM) && userConfigs.get(SaslConfigs.SASL_MECHANISM).equals("PLAIN"))) {
                props.put(SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS, config.saslLoginCallbackClass);
            }
        }*/
        props.putAll(userConfigs);
        return props;
    }

    public String getTopic() {
        return topic;
    }

    public String getAutoOffsetReset() { // not used in KafkaConsumerExample
        return autoOffsetReset;
    }

    public String getEnableAutoCommit() {
        return enableAutoCommit;
    }

    public Long getMessageCount() {
        return messageCount;
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

    public TracingSystem getTracingSystem() {
        return tracingSystem;
    }


    @Override
    public String toString() {
        return "KafkaConsumerConfig{" +
                "topic='" + topic + '\'' +
                ", autoOffsetReset='" + autoOffsetReset + '\'' +
                ", enableAutoCommit='" + enableAutoCommit + '\'' +
                ", messageCount=" + messageCount +
                ", oauthClientId='" + oauthClientId + '\'' +
                ", oauthClientSecret='" + oauthClientSecret + '\'' +
                ", oauthAccessToken='" + oauthAccessToken + '\'' +
                ", oauthRefreshToken='" + oauthRefreshToken + '\'' +
                ", oauthTokenEndpointUri='" + oauthTokenEndpointUri + '\'' +
                ", tracingSystem='" + tracingSystem + '\'' +
                kafkaFieldsToString() +
                '}';
    }

    public static String kafkaFieldsToString() {
        StringBuilder sb = new StringBuilder();
        Map<String, String> envVars = System.getenv();
        for (Map.Entry<String, String> entry : envVars.entrySet()) {
            if (entry.getKey().contains("KAFKA")) {
                String key = convertEnvVarToPropertyKey(entry.getKey());
                String value = entry.getValue();
                sb.append(", " + key + "='" + value + "\'");
            }
        }
        return sb.toString();
    }

   /* public static void main(String[] args) {
        KafkaConsumerConfig KCC = KafkaConsumerConfig.fromEnv();
        Properties props = createProperties(KCC);
        System.out.println("\n" + KCC.toString()); // prints the properties that have been passed to toString()
        System.out.println("These are the props: " + props);
    }*/
}