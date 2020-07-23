/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import io.jaegertracing.Configuration;
import io.opentracing.Tracer;
import io.opentracing.contrib.kafka.TracingConsumerInterceptor;
import io.opentracing.contrib.kafka.TracingProducerInterceptor;
import io.opentracing.util.GlobalTracer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.header.Header;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Collections;
import java.util.Properties;

public class KafkaConsumerExample {
    private static final Logger log = LogManager.getLogger(KafkaConsumerExample.class);

    public static void main(String[] args) {
        KafkaConsumerConfig config = KafkaConsumerConfig.fromEnv();
        Properties props = KafkaConsumerConfig.createProperties(config);
        int receivedMsgs = 0;

        if (System.getenv("JAEGER_SERVICE_NAME") != null)   {
            Tracer tracer = Configuration.fromEnv().getTracer();
            GlobalTracer.registerIfAbsent(tracer);

            props.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingConsumerInterceptor.class.getName());
        }

        boolean commit = !Boolean.parseBoolean(config.getEnableAutoCommit());
        KafkaConsumer consumer = new KafkaConsumer(props);
        consumer.subscribe(Collections.singletonList(config.getTopic()));

        while (receivedMsgs < config.getMessageCount()) {
            ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
            for (ConsumerRecord<String, String> record : records) {
                log.info("Received message:");
                log.info("\tpartition: {}", record.partition());
                log.info("\toffset: {}", record.offset());
                log.info("\tvalue: {}", record.value());
                if (record.headers() != null) {
                    log.info("\theaders: ");
                    for (Header header : record.headers().toArray()) {
                        log.info("\tkey: {}, value: {}", header.key(), new String(header.value()));
                    }
                }
                receivedMsgs++;
            }
            if (commit) {
                consumer.commitSync();
            }
        }
    }
}
