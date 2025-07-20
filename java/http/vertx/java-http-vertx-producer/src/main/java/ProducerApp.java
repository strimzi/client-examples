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

public final class ProducerApp {

    private static final Logger log = LogManager.getLogger(ProducerApp.class);

    public static void main(String[] args) throws Exception {
        HttpKafkaProducerConfig config = HttpKafkaProducerConfig.fromEnv();
        CountDownLatch messagesSentLatch = new CountDownLatch(1);
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

        HttpKafkaProducer httpKafkaProducer = new HttpKafkaProducer(config,  messagesSentLatch);

        vertx.deployVerticle(httpKafkaProducer)
                .onSuccess(deploymentId -> {
                    log.info("HTTP Kafka producer verticle started successfully [{}]", deploymentId);
                })
                .onFailure(t -> {
                    log.error("Failed to deploy HTTP Kafka producer verticle", t);
                    System.exit(1);
                });

        log.info("Waiting for sending all messages");
        messagesSentLatch.await();
        vertx.close().onComplete(v -> exitLatch.countDown());
        log.info("Waiting HTTP producer verticle to be closed");
        boolean releaseBeforeTimeout = exitLatch.await(60000, TimeUnit.MILLISECONDS);
        log.info("Latch released before Timeout: {}", releaseBeforeTimeout);
    }
}