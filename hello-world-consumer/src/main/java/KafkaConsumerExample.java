import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

public class KafkaConsumerExample {
    private static final Logger log = LogManager.getLogger(KafkaConsumerExample.class);

    public static void main(String[] args) {
        KafkaConsumerConfig config = KafkaConsumerConfig.fromEnv();
        Properties props = KafkaConsumerConfig.createProperties(config);
        int receivedMsgs = 0;
        boolean commit = !Boolean.parseBoolean(config.getEnableAutoCommit());
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        log.info("Subscribing to topic {}", config.getTopic());
        //consumer.subscribe(Collections.singletonList(config.getTopic()));
        consumer.assign(Collections.singletonList(new TopicPartition("my-topic", 0)));
        Duration timeout = Duration.ofMinutes(1);
        if (log.isInfoEnabled()) {
            Set<TopicPartition> assignment = consumer.assignment();
            log.info("Assigned to partitions {}", assignment);
            for (TopicPartition tp : assignment) {
                consumer.position(tp, timeout);
            }
        }

        try {
            while (receivedMsgs < config.getMessageCount()) {
                ConsumerRecords<String, String> records = consumer.poll(timeout);
                for (ConsumerRecord<String, String> record : records) {
                    log.info("Received message:");
                    log.info("\tpartition: {}", record.partition());
                    log.info("\toffset: {}", record.offset());
                    log.info("\tvalue: {}", record.value());
                    if (commit) {
                        consumer.commitSync();
                    }
                    receivedMsgs++;
                }
            }
        } finally {
            log.info("Received {} messages in total", receivedMsgs);
        }

    }
}
