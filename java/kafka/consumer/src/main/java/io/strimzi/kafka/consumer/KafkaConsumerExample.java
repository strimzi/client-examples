/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */


package io.strimzi.kafka.consumer;
import io.strimzi.common.TracingInitializer;
import io.strimzi.common.TracingSystem;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.header.Header;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;

public class KafkaConsumerExample {
    private static final Logger log = LogManager.getLogger(KafkaConsumerExample.class);

    public static void main(String[] args) {
        KafkaConsumerConfig config = KafkaConsumerConfig.fromEnv();

        log.info(KafkaConsumerConfig.class.getName() + ": {}", config.toString());

        Properties props = config.getProperties();
        int receivedMsgs = 0;

        TracingSystem tracingSystem = config.getTracingSystem();
        if (tracingSystem != TracingSystem.NONE) {
            if (tracingSystem == TracingSystem.JAEGER) {
                TracingInitializer.jaegerInitialize();

                props.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, io.opentracing.contrib.kafka.TracingConsumerInterceptor.class.getName());
            } else if (tracingSystem == TracingSystem.OPENTELEMETRY) {
                TracingInitializer.otelInitialize();

                props.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, io.opentelemetry.instrumentation.kafkaclients.TracingConsumerInterceptor.class.getName());
            } else {
                log.error("Error: TRACING_SYSTEM {} is not recognized or supported!", tracingSystem);
            }
        }

        boolean commit = !Boolean.parseBoolean(props.getProperty(ENABLE_AUTO_COMMIT_CONFIG));
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(config.getTopic()));

        while (receivedMsgs < config.getMessageCount()) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(Long.MAX_VALUE));
            for (ConsumerRecord<String, String> record : records) {
                log.info("Received message:");
                log.info("\tpartition: {}", record.partition());
                log.info("\toffset: {}", record.offset());
                log.info("\tvalue: {}", record.value());
                if (record.headers() != null) {
                    log.info("\theaders: ");
                    for (Header header : record.headers()) {
                        log.info("\t\tkey: {}, value: {}", header.key(), new String(header.value()));
                    }
                }
                receivedMsgs++;
            }
            if (commit) {
                consumer.commitSync();
            }
        }
        log.info("Received {} messages", receivedMsgs);
    }
}
