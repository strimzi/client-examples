import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Properties;

public class KafkaProducerExample {
    private static final Logger log = LogManager.getLogger(KafkaProducerExample.class);

    public static void main(String[] args) throws InterruptedException {
        KafkaProducerConfig config = KafkaProducerConfig.fromEnv();

        Properties props = new Properties();
        props.put("bootstrap.servers", config.getBootstrapServers());
        props.put("acks", config.getAcks());
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

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

        KafkaProducer producer = new KafkaProducer(props);
        log.info("Sending {} messages ...", config.getMessageCount());
        for (long i = 0; i < config.getMessageCount(); i++) {
            log.info("Sending messages \"Hello world - {}\"", i);
            producer.send(new ProducerRecord(config.getTopic(), getKey(i, config.getNumberOfKeys()),  "Hello world - " + i));
            Thread.sleep(config.getTimer());
        }
        log.info("{} messages sent ...", config.getMessageCount());
        producer.close();
    }

    private static String getKey(long sentMessages, long numberOfKeys) {
        return "key-" + sentMessages % numberOfKeys;
    }
}
