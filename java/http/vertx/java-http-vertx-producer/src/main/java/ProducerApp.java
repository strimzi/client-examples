/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.tracing.opentelemetry.OpenTelemetryOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.strimzi.common.TracingSystem;

public final class ProducerApp {

    private static final Logger log = LogManager.getLogger(ProducerApp.class);

    public static void main(String[] args) throws Exception {
        CountDownLatch messagesSentLatch = new CountDownLatch(1);
        CountDownLatch exitLatch = new CountDownLatch(1);

        Map<String, Object> objectMap = new HashMap<>();
        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            objectMap.put(entry.getKey(), entry.getValue());
        }
        HttpKafkaProducerConfig httpKafkaProducerConfig = HttpKafkaProducerConfig.fromMap(objectMap);
        HttpKafkaProducer httpKafkaProducer = new HttpKafkaProducer(httpKafkaProducerConfig,  messagesSentLatch);

        TracingSystem tracingSystem = httpKafkaProducerConfig.getTracingSystem();
        VertxOptions vertxOptions = new VertxOptions();
        if (tracingSystem != TracingSystem.NONE) {
            if (tracingSystem == TracingSystem.OPENTELEMETRY) {
                vertxOptions.setTracingOptions(new OpenTelemetryOptions());
            } else {
                log.error("Error: STRIMZI_TRACING_SYSTEM {} is not recognized or supported!", httpKafkaProducerConfig.getTracingSystem());
            }
        }
        Vertx vertx = Vertx.vertx(vertxOptions);

        vertx.deployVerticle(httpKafkaProducer, done -> {
            if (done.succeeded()) {
                log.info("HTTP Kafka producer started successfully");
            } else {
                log.error("Failed to deploy HTTP Kafka producer", done.cause());
                System.exit(1);
            }
        });

        log.info("Waiting for sending all messages");
        messagesSentLatch.await();
        vertx.close(done -> exitLatch.countDown());
        log.info("Waiting HTTP producer verticle to be closed");
        boolean releaseBeforeTimeout = exitLatch.await(60000, TimeUnit.MILLISECONDS);
        log.info("Latch released before Timeout: {}", releaseBeforeTimeout);
    }
}