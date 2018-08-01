import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Collections;
import java.util.Properties;

public class KafkaConsumerExample {
    private static final Logger log = LogManager.getLogger(KafkaConsumerExample.class);

    public static void main(String[] args) {
        KafkaConsumerConfig config = KafkaConsumerConfig.fromEnv();
        boolean commit;
        int receivedMsgs = 0;
        Properties props = new Properties();
        props.put("bootstrap.servers", config.getBootstrapServers());
        props.put("group.id", config.getGroupId());
        props.put("auto.offset.reset", config.getAutoOffsetReset());
        props.put("enable.auto.commit", config.getEnableAutoCommit());
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        if (config.getTrustStorePassword() != null && config.getTrustStorePath() != null)   {
            log.info("Configuring truststore");
            props.put("security.protocol", "SSL");
            props.put("ssl.truststore.type", "PKCS12");
            props.put("ssl.truststore.password", config.getTrustStorePassword());
            props.put("ssl.truststore.location", config.getTrustStorePath());
        }

        if (config.getKeyStorePassword() != null && config.getKeyStorePath() != null)   {
            log.info("Configuring keystore");
            props.put("security.protocol", "SSL");
            props.put("ssl.keystore.type", "PKCS12");
            props.put("ssl.keystore.password", config.getKeyStorePassword());
            props.put("ssl.keystore.location", config.getKeyStorePath());
        }

        commit = !Boolean.parseBoolean(config.getEnableAutoCommit());
        KafkaConsumer consumer = new KafkaConsumer(props);
        consumer.subscribe(Collections.singletonList(config.getTopic()));
        while (receivedMsgs < config.getMessageCount()) {
            ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
            for (ConsumerRecord<String, String> record : records) {
                log.info("Received message:");
                log.info("\tpartition: " + record.partition());
                log.info("\toffset: " + record.offset());
                log.info("\tvalue: " + record.value());
                if (commit) {
                    consumer.commitSync();
                }
                receivedMsgs++;
            }
        }
    }
}
