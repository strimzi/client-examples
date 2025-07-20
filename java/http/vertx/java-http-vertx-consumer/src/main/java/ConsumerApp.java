/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.tracing.opentelemetry.OpenTelemetryOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.strimzi.common.TracingSystem;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class ConsumerApp {

    private static final Logger log = LogManager.getLogger(ConsumerApp.class);

    public static void main(String[] args) throws Exception {
        HttpKafkaConsumerConfig config = HttpKafkaConsumerConfig.fromEnv();
        CountDownLatch messagesReceivedLatch = new CountDownLatch(1);
        CountDownLatch exitLatch = new CountDownLatch(1);

        TracingSystem tracingSystem = config.getTracingSystem();
        VertxOptions vertxOptions = new VertxOptions();
        if (tracingSystem != TracingSystem.NONE) {
            if (tracingSystem == TracingSystem.OPENTELEMETRY) {
                vertxOptions.setTracingOptions(new OpenTelemetryOptions());
            } else {
                log.error("Error: STRIMZI_TRACING_SYSTEM {} is not recognized or supported!", config.getTracingSystem());
            }
        }
        Vertx vertx = Vertx.vertx(vertxOptions);

        HttpKafkaConsumer httpKafkaConsumer = new HttpKafkaConsumer(config, messagesReceivedLatch);

        vertx.deployVerticle(httpKafkaConsumer)
                .onSuccess(deploymentId -> {
                    log.info("HTTP Kafka consumer verticle started successfully [{}]", deploymentId);
                })
                .onFailure(t -> {
                    log.error("Failed to deploy HTTP Kafka consumer verticle", t);
                    System.exit(1);
                });

        log.info("Waiting for receiving all messages");
        messagesReceivedLatch.await();
        vertx.close().onComplete(v -> exitLatch.countDown());
        log.info("Waiting HTTP consumer verticle to be closed");
        boolean releaseBeforeTimeout = exitLatch.await(60000, TimeUnit.MILLISECONDS);
        log.info("Latch released before Timeout: {}", releaseBeforeTimeout);
    }
}