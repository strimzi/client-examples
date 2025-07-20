/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * HttpKafkaProducer
 */
public class HttpKafkaProducer extends AbstractVerticle {

    private final static Logger log = LogManager.getLogger(HttpKafkaProducer.class);

    private final HttpKafkaProducerConfig config;

    private WebClient client;
    private long sendTimer;
    private int messagesSent;

    private CountDownLatch messagesSentLatch;

    /**
     * Constructor
     *
     * @param config configuration
     * @param messagesSentLatch latch to set when the number of requested messaged are sent
     */
    public HttpKafkaProducer(HttpKafkaProducerConfig config, CountDownLatch messagesSentLatch) {
        this.config = config;
        this.messagesSentLatch = messagesSentLatch;
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        log.info("HTTP Kafka producer starting with config {}", this.config);
        WebClientOptions options = new WebClientOptions()
                .setDefaultHost(this.config.getHostname())
                .setDefaultPort(this.config.getPort())
                .setTracingPolicy(TracingPolicy.ALWAYS);

        this.client = WebClient.create(vertx, options);
        this.sendTimer = vertx.setPeriodic(this.config.getDelay(), t -> {
            this.send(this.config.getTopic()).future().onComplete(ar -> {
                if (ar.succeeded()) {
                    log.info("Sent {}", ar.result());
                }
            });
        });
        startPromise.complete();
    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        log.info("HTTP Kafka producer stopping");
        this.vertx.cancelTimer(this.sendTimer);
        stopPromise.complete();
    }

    private Promise<List<OffsetRecordSent>> send(String topic) {
        Promise<List<OffsetRecordSent>> fut = Promise.promise();

        JsonObject records = new JsonObject();
        records.put("records", new JsonArray().add(new JsonObject().put("value", "message-" + this.messagesSent++)));

        HttpRequest<Buffer> httpRequest = this.client.post(this.config.getEndpointPrefix() + "/topics/" + topic);

        httpRequest
                .putHeader(HttpHeaderNames.CONTENT_LENGTH.toString(), String.valueOf(records.toBuffer().length()))
                .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/vnd.kafka.json.v2+json")
                .as(BodyCodec.jsonObject())
                .sendJsonObject(records)
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        HttpResponse<JsonObject> response = ar.result();
                        if (response.statusCode() == HttpResponseStatus.OK.code()) {
                            List<OffsetRecordSent> list = new ArrayList<>();
                            response.body().getJsonArray("offsets").forEach(obj -> {
                                JsonObject json = (JsonObject) obj;
                                list.add(new OffsetRecordSent(
                                        json.getInteger("partition"),
                                        json.getLong("offset"))
                                );
                            });
                            fut.complete(list);
                        } else {
                            fut.fail(new RuntimeException("Got HTTP status code " + response.statusCode()));
                        }
                    } else {
                        fut.fail(ar.cause());
                    }

                    if (this.config.getMessageCount().isPresent() &&
                            this.messagesSent >= this.config.getMessageCount().get()) {
                        // signal to main thread that all messages are sent, application can exit
                        this.messagesSentLatch.countDown();
                        log.info("All messages sent");
                    }
                });
        return fut;
    }

    /**
     * Represents information about a message sent
     */
    static class OffsetRecordSent {

        private final int partition;
        private final long offset;

        OffsetRecordSent(int partition, long offset) {
            this.partition = partition;
            this.offset = offset;
        }

        /**
         * @return partition from which the message was received
         */
        public int getPartition() {
            return partition;
        }

        /**
         * @return message offset in the partition
         */
        public long getOffset() {
            return offset;
        }

        @Override
        public String toString() {
            return "OffsetRecordSent(" +
                    "partition=" + this.partition +
                    ",offset=" + this.offset +
                    ")";
        }
    }
}