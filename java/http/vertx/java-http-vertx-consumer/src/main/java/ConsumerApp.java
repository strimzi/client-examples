/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.jaegertracing.Configuration;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public final class ConsumerApp {

    private static final Logger log = LogManager.getLogger(ConsumerApp.class);
    
    private static String deploymentId;

    public static void main(String[] args) throws Exception {
        Vertx vertx = Vertx.vertx();

        CountDownLatch messagesReceivedLatch = new CountDownLatch(1);
        CountDownLatch exitLatch = new CountDownLatch(1);

        ConfigStoreOptions envStore = new ConfigStoreOptions()
                .setType("env")
                .setConfig(new JsonObject().put("raw-data", true));

        ConfigRetrieverOptions options = new ConfigRetrieverOptions()
                .addStore(envStore);

        ConfigRetriever retriever = ConfigRetriever.create(vertx, options);

        retriever.getConfig(ar -> {
            Map<String, Object> envConfig = ar.result().getMap();
            HttpKafkaConsumerConfig httpKafkaConsumerConfig = HttpKafkaConsumerConfig.fromMap(envConfig);

            HttpKafkaConsumer httpKafkaConsumer = new HttpKafkaConsumer(httpKafkaConsumerConfig, messagesReceivedLatch);

            vertx.deployVerticle(httpKafkaConsumer, done -> {
                if (done.succeeded()) {
                    deploymentId = done.result();

                    if (envConfig.get("JAEGER_SERVICE_NAME") != null) {
                        Tracer tracer = Configuration.fromEnv().getTracer();
                        GlobalTracer.registerIfAbsent(tracer);
                    }

                    log.info("HTTP Kafka consumer started successfully");
                } else {
                    log.error("Failed to deploy HTTP Kafka consumer", done.cause());
                    System.exit(1);
                }
            });
        });

        log.info("Waiting for receiveing all messages");
        messagesReceivedLatch.await();
        vertx.close(done -> exitLatch.countDown());
        log.info("Waiting HTTP consumer verticle to be closed");
        boolean releaseBeforeTimeout = exitLatch.await(60000, TimeUnit.MILLISECONDS);
        log.info("Latch released before Timeout: {}", releaseBeforeTimeout);
    }
}
