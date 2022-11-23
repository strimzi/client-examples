/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import static java.util.Map.entry;

public class KafkaProducerConfig {
    private static final Logger log = LogManager.getLogger(KafkaProducerConfig.class);

    private static final long DEFAULT_MESSAGES_COUNT = 10;
    private static final String DEFAULT_MESSAGE = "Hello world";

    private final String topic;
    private final int delay;
    private final Long messageCount;
    private final String message;
    private final String headers;
    private final String additionalConfig;
    private final String saslLoginCallbackClass = "io.strimzi.kafka.oauth.client.JaasClientOauthLoginCallbackHandler";
    private final TracingSystem tracingSystem;

    private static final Map<String, String> DEFAULT_MAP = Map.ofEntries(
            entry(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer"),
            entry(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer"),
            entry(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL"),
            entry(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "PEM"),
            entry(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PEM"),
            entry(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required"),
            //  entry(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL"),
            entry(SaslConfigs.SASL_MECHANISM, "OAUTHBEARER")
            //  entry(SaslConfigs.SASL_MECHANISM, "PLAIN")
    );

    public KafkaProducerConfig(String topic, int delay, Long messageCount, String message,
                             String headers, String additionalConfig, TracingSystem tracingSystem) {
        this.topic = topic;
        this.delay = delay;
        this.messageCount = messageCount;
        this.message = message;
        this.headers = headers;
        this.additionalConfig = additionalConfig;
        this.tracingSystem = tracingSystem;
    }

    public static KafkaProducerConfig fromEnv() {
        String topic = System.getenv("TOPIC");
        int delay = Integer.parseInt(System.getenv("DELAY_MS"));
        Long messageCount = System.getenv("MESSAGE_COUNT") == null ? DEFAULT_MESSAGES_COUNT : Long.parseLong(System.getenv("MESSAGE_COUNT"));
        String message = System.getenv("MESSAGE") == null ? DEFAULT_MESSAGE : System.getenv("MESSAGE");

        String headers = System.getenv("HEADERS");
        String additionalConfig = System.getenv().getOrDefault("ADDITIONAL_CONFIG", "");
        TracingSystem tracingSystem = TracingSystem.forValue(System.getenv().getOrDefault("TRACING_SYSTEM", ""));

        return new KafkaProducerConfig(topic, delay, messageCount, message, additionalConfig, headers, tracingSystem);
    }
    public static String convertEnvVarToPropertyKey(String propKey) {
        propKey = propKey.substring(6).toLowerCase().replace("_", ".");
        return propKey;
    }


    public static Properties createProperties(KafkaProducerConfig config) {
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
                props.put(key, value);
                System.out.println("Trap 2 key: " + key + " value: " + value);
            }
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

       /* if ((config.getOauthAccessToken() != null)
                || (config.getOauthTokenEndpointUri() != null && config.getOauthClientId() != null && config.getOauthRefreshToken() != null)
                || (config.getOauthTokenEndpointUri() != null && config.getOauthClientId() != null && config.getOauthClientSecret() != null))    {
            log.info("Configuring OAuth");
            props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;");
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL".equals(props.getProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG)) ? "SASL_SSL" : "SASL_PLAINTEXT");
            props.put(SaslConfigs.SASL_MECHANISM, "OAUTHBEARER");
            if (!(additionalProps.containsKey(SaslConfigs.SASL_MECHANISM) && additionalProps.getProperty(SaslConfigs.SASL_MECHANISM).equals("PLAIN"))) {
                props.put(SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS, config.saslLoginCallbackClass);
            }
        }*/

        // override properties with defined additional properties
        props.putAll(additionalProps);

        return props;
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
            ", topic='" + topic + '\'' +
            ", delay=" + delay +
            ", messageCount=" + messageCount +
            ", message='" + message + '\'' +
            ", headers='" + headers + '\'' +
            ", additionalConfig='" + additionalConfig + '\'' +
            ", tracingSystem='" + tracingSystem + '\'' +
            '}';
    }
}
