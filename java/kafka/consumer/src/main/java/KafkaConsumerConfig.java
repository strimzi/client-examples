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
import java.util.Set;
import java.util.StringTokenizer;

import static java.util.Map.entry;
import static org.apache.kafka.common.requests.DeleteAclsResponse.log;

public class KafkaConsumerConfig {
    private static final long DEFAULT_MESSAGES_COUNT = 10;

    private final String topic;
    private final String autoOffsetReset = "earliest"; // ??
    private final String enableAutoCommit = "false"; // ??
    private final Long messageCount;
    private final TracingSystem tracingSystem;
    private final String additionalConfig;

    private static final Map<String, String> DEFAULT_MAP = Map.ofEntries(
            entry(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer"),
            entry(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer"),
            entry(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL"),
            entry(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "PEM")
            // entry(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PEM")
         //   entry(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required"),
          //  entry(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL"),
         //   entry(SaslConfigs.SASL_MECHANISM, "OAUTHBEARER")
          //  entry(SaslConfigs.SASL_MECHANISM, "PLAIN")
    );

    private static final Set<String> CA_CERT_FIELDS = Set.of(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, SslConfigs.SSL_TRUSTSTORE_CERTIFICATES_CONFIG);
    private static final Set<String> USER_CERT_FIELDS = Set.of(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, SslConfigs.SSL_KEYSTORE_CERTIFICATE_CHAIN_CONFIG, SslConfigs.SSL_KEYSTORE_KEY_CONFIG);

    public KafkaConsumerConfig(String topic, Long messageCount, TracingSystem tracingSystem, String additionalConfig) {
        this.topic = topic;
        this.messageCount = messageCount;
        this.tracingSystem = tracingSystem;
        this.additionalConfig = additionalConfig;
    }

    public static KafkaConsumerConfig fromEnv() {
        String topic = System.getenv("TOPIC");
        Long messageCount = System.getenv("MESSAGE_COUNT") == null ? DEFAULT_MESSAGES_COUNT : Long.parseLong(System.getenv("MESSAGE_COUNT"));
        TracingSystem tracingSystem = TracingSystem.forValue(System.getenv().getOrDefault("TRACING_SYSTEM", ""));
        String additionalConfig = System.getenv().getOrDefault("ADDITIONAL_CONFIG", "");

        return new KafkaConsumerConfig(topic, messageCount, tracingSystem, additionalConfig);
    }

    public static String convertEnvVarToPropertyKey(String propKey) {
        propKey = propKey.substring(6).toLowerCase().replace("_", ".");
        return propKey;
    }

    public static Properties createProperties(KafkaConsumerConfig config) {
        Properties props = new Properties();
        System.out.println("Printing translated key/value pairs");
        Map<String, String> envVars = System.getenv();
        for (Map.Entry<String, String> entry : envVars.entrySet()) {
            if (entry.getKey().contains("KAFKA")) {
                String key = convertEnvVarToPropertyKey(entry.getKey());
                String value = entry.getValue();
                System.out.println("key: " + key + " value: " + value);
                props.put(key, value);
            }
        }
        System.out.println("Trap 0 " + DEFAULT_MAP);
        for (Map.Entry<String, String> entry : DEFAULT_MAP.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            System.out.println("Trap 1 key: " + key + " value: " + value);

            if (props.get(key) == null) {
                if (CA_CERT_FIELDS.contains(key) && props.get("ca.cert") != null) {
                    props.put(key, value);
                }
                else if (USER_CERT_FIELDS.contains(key) && props.get("user.cert") != null) {
                    props.put(key, value);
                } else {
                    props.put(key, value);
                    System.out.println("Trap 2 key: " + key + " value: " + value);
                }
            }
        }

/*

        Properties additionalProps = new Properties();
        for (Map.Entry<String, String> entry : DEFAULT_MAP.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
        additionalProps.put(key, value);
        }
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


        props.putAll(additionalProps);*/
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

    public TracingSystem getTracingSystem() {
        return tracingSystem;
    }

    public String getAdditionalConfig() {
        return additionalConfig;
    }


    @Override
    public String toString() {
        return "KafkaConsumerConfig{" +
                "topic='" + topic + '\'' +
                ", autoOffsetReset='" + autoOffsetReset + '\'' +
                ", enableAutoCommit='" + enableAutoCommit + '\'' +
                ", messageCount=" + messageCount +
                ", tracingSystem='" + tracingSystem + '\'' +
                ", additionalConfig='" + additionalConfig + '\'' +
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

    public static void main(String[] args) {
        KafkaConsumerConfig KCC = KafkaConsumerConfig.fromEnv();
        Properties props = createProperties(KCC);
        System.out.println("\n" + KCC.toString()); // prints the properties that have been passed to toString()
        System.out.println("These are the props: " + props);
    }
}


// TODO add specific defaults to props object
               /* if (props.get("ca.cert") != null && (
                        key == CommonClientConfigs.SECURITY_PROTOCOL_CONFIG
                                || key == SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG
                                || key == SslConfigs.SSL_TRUSTSTORE_CERTIFICATES_CONFIG)) {
                    props.put(key, value);
                }*/
  /*              // TODO add specific defaults to props object
                if (props.get("user.cert") != null && (
                        key == CommonClientConfigs.SECURITY_PROTOCOL_CONFIG
                                || key == SslConfigs.SSL_KEYSTORE_TYPE_CONFIG
                                || key == SslConfigs.SSL_KEYSTORE_CERTIFICATE_CHAIN_CONFIG
                                || key == SslConfigs.SSL_KEYSTORE_KEY_CONFIG)) {
                    props.put(key, value);
                }
            }
            */