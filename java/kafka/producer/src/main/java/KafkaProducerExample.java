/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

public class KafkaProducerExample {
    private static final Logger log = LogManager.getLogger(KafkaProducerExample.class);

    public static void main(String[] args) throws InterruptedException {
        KafkaProducerConfig config = KafkaProducerConfig.fromEnv();

        log.info(KafkaProducerConfig.class.getName() + ": {}", config.toString());

        Properties props = config.getProperties();
        List<Header> headers = null;

        TracingSystem tracingSystem = config.getTracingSystem();
        if (tracingSystem != TracingSystem.NONE) {
            if (tracingSystem == TracingSystem.JAEGER) {
                TracingInitializer.jaegerInitialize();

                props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, io.opentracing.contrib.kafka.TracingProducerInterceptor.class.getName());
            } else if (tracingSystem == TracingSystem.OPENTELEMETRY) {
                TracingInitializer.otelInitialize();

                props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, io.opentelemetry.instrumentation.kafkaclients.TracingProducerInterceptor.class.getName());
            } else {
                log.error("Error: STRIMZI_TRACING_SYSTEM {} is not recognized or supported!", config.getTracingSystem());
            }
        }

        if (config.getHeaders() != null) {
            headers = new ArrayList<>();

            String[] headersArray = config.getHeaders().split(", [\t\n\r]?");
            for (String header : headersArray) {
                headers.add(new RecordHeader(header.split("=")[0], header.split("=")[1].getBytes()));
            }
        }

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        boolean blockProducer = System.getenv("BLOCKING_PRODUCER") != null;
        boolean transactionalProducer = System.getenv("KAFKA_TRANSACTIONAL_ID") != null;
        int msgPerTx = Integer.parseInt(System.getenv().getOrDefault("MESSAGES_PER_TRANSACTION", "10"));
        if(transactionalProducer) {
            log.info("Using transactional producer. Initializing the transactions ...");
            producer.initTransactions();
        }
        AtomicLong numSent = new AtomicLong(0);
        for (long i = 0; i < config.getMessageCount(); i++) {
            if (transactionalProducer && i % msgPerTx == 0) {
                log.info("Beginning new transaction. Messages sent: {}", i);
                producer.beginTransaction();
            }
            log.info("Sending messages \"" + config.getMessage() + " - {}\"{}", i, config.getHeaders() == null ? "" : " - with headers - " + config.getHeaders());
            Future<RecordMetadata> recordMetadataFuture = producer.send(new ProducerRecord<>(config.getTopic(), null, null, null, "\"" + config.getMessage() + " - " + i + "\"", headers));
            if(blockProducer) {
                try {
                    recordMetadataFuture.get();
                    // Increment number of sent messages only if ack is received by producer
                    numSent.incrementAndGet();
                } catch (ExecutionException e) {
                    log.warn("Message {} wasn't sent properly!", i, e.getCause());
                }
            } else {
                // Increment number of sent messages for non-blocking producer
                numSent.incrementAndGet();
            }
            if (transactionalProducer && ((i + 1) % msgPerTx == 0)) {
                log.info("Committing the transaction. Messages sent: {}", i);
                producer.commitTransaction();
            }

            Thread.sleep(config.getDelay());
        }

        log.info("{} messages sent ...", numSent.get());
        producer.close();
        System.exit(numSent.get() == config.getMessageCount() ? 0 : 1);
    }
}
