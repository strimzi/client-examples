/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.MultiMap;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * HttpKafkaConsumer
 */
public class HttpKafkaConsumer extends AbstractVerticle {

    private final static Logger log = LogManager.getLogger(HttpKafkaConsumer.class);

    private final HttpKafkaConsumerConfig config;

    private WebClient client;
    private CreatedConsumer consumer;
    private long pollTimer;
    private int messagesReceived;

    private CountDownLatch messagesReceivedLatch;

    /**
     * Constructor
     * 
     * @param config configuration
     * @param messagesReceivedLatch latch to set when the number of requested messaged are received
     */
    public HttpKafkaConsumer(HttpKafkaConsumerConfig config, CountDownLatch messagesReceivedLatch) {
        this.config = config;
        this.messagesReceivedLatch = messagesReceivedLatch;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        log.info("HTTP Kafka consumer starting with config {}", this.config);

        WebClientOptions options = new WebClientOptions()
                .setDefaultHost(this.config.getHostname())
                .setDefaultPort(this.config.getPort())
                .setPipelining(this.config.isPipelining())
                .setPipeliningLimit(this.config.getPipeliningLimit());
        this.client = WebClient.create(vertx, options);
        
        this.createConsumer()
        .compose(consumer -> this.subscribe(consumer, this.config.getTopic()))
        .compose(v -> {
            this.pollTimer = vertx.setPeriodic(this.config.getPollInterval(), t -> {
                this.poll().setHandler(ar -> {
                    if (ar.succeeded()) {
                        log.info("Received {}", ar.result());
                    }
                });
            });
            startFuture.complete();
        }, startFuture);
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        log.info("HTTP Kafka consumer stopping");
        if (this.consumer != null) {
            this.vertx.cancelTimer(this.pollTimer);
            this.deleteConsumer().setHandler(ar -> {
                stopFuture.complete();
            });
        } else {
            stopFuture.complete();
        }
    }

    private Future<CreatedConsumer> createConsumer() {
        Future<CreatedConsumer> fut = Future.future();

        JsonObject json = new JsonObject()
            .put("format", "json");
        
        if (config.getClientId() != null) {
            json.put("name", config.getClientId());
        }

        this.client.post(this.config.getEndpointPrefix() + "/consumers/" + this.config.getGroupid())
            .putHeader(HttpHeaderNames.CONTENT_LENGTH.toString(), String.valueOf(json.toBuffer().length()))
            .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/vnd.kafka.v2+json")
            .as(BodyCodec.jsonObject())
            .sendJsonObject(json, ar -> {
                if (ar.succeeded()) {
                    HttpResponse<JsonObject> response = ar.result();
                    if (response.statusCode() == HttpResponseStatus.OK.code()) {
                        JsonObject body = response.body();
                        this.consumer = new CreatedConsumer(body.getString("instance_id"), body.getString("base_uri"));
                        log.info("Consumer created as {}", this.consumer);
                        fut.complete(consumer);
                    } else {
                        fut.fail(new RuntimeException("Got HTTP status code " + response.statusCode()));
                    }
                } else {
                    fut.fail(ar.cause());
                }
            });
        return fut;
    }

    private Future<Void> subscribe(CreatedConsumer consumer, String topic) {
        Future<Void> fut = Future.future();

        JsonObject topics = new JsonObject()
            .put("topics", new JsonArray().add(topic));
        
        this.client.post(consumer.getBaseUri() + "/subscription")
            .putHeader(HttpHeaderNames.CONTENT_LENGTH.toString(), String.valueOf(topics.toBuffer().length()))
            .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/vnd.kafka.v2+json")
            .as(BodyCodec.jsonObject())
            .sendJsonObject(topics, ar -> {
                if (ar.succeeded()) {
                    HttpResponse<JsonObject> response = ar.result();
                    if (response.statusCode() == HttpResponseStatus.NO_CONTENT.code()) {
                        log.info("Subscribed to {}", topic);
                        fut.complete();
                    } else {
                        fut.fail(new RuntimeException("Got HTTP status code " + response.statusCode()));
                    }
                } else {
                    fut.fail(ar.cause());
                }
            });
        return fut;
    }

    private Future<List<ConsumerRecord>> poll() {
        Future<List<ConsumerRecord>> fut = Future.future();        

        log.info("Poll ...");
        this.client.get(this.consumer.getBaseUri() + "/records?timeout=" + this.config.getPollTimeout())
            .putHeader(HttpHeaderNames.ACCEPT.toString(), "application/vnd.kafka.json.v2+json")
            .as(BodyCodec.jsonArray())
            .send(ar -> {
                if (ar.succeeded()) {
                    HttpResponse<JsonArray> response = ar.result();
                    if (response.statusCode() == HttpResponseStatus.OK.code()) {

                        Tracer tracer = GlobalTracer.get();

                        MultiMap rawHeaders = response.headers();
                        final Map<String, String> headers = new HashMap<>();
                        for (Map.Entry<String, String> header : rawHeaders) {
                            headers.put(header.getKey(), header.getValue());
                        }

                        String operation = "poll";
                        Tracer.SpanBuilder spanBuilder;
                        try {
                            SpanContext parentSpan = tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapAdapter(headers));
                            if (parentSpan == null) {
                                spanBuilder = tracer.buildSpan(operation);
                            } else {
                                spanBuilder = tracer.buildSpan(operation).asChildOf(parentSpan);
                            }
                        } catch (IllegalArgumentException e) {
                            spanBuilder = tracer.buildSpan(operation);
                        }

                        Span span = spanBuilder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER).start();

                        List<ConsumerRecord> list = new ArrayList<>();
                        response.body().forEach(obj -> {
                            JsonObject json = (JsonObject) obj;
                            list.add(new ConsumerRecord(
                                json.getString("topic"), 
                                json.getValue("key"),
                                json.getValue("value"),
                                json.getInteger("partition"), 
                                json.getLong("offset"))
                                );
                        });
                        this.messagesReceived += list.size();

                        span.finish();

                        fut.complete(list);
                    } else {
                        fut.fail(new RuntimeException("Got HTTP status code " + response.statusCode()));
                    }
                } else {
                    fut.fail(ar.cause());
                }

                if (this.config.getMessageCount().isPresent() &&
                    this.messagesReceived >= this.config.getMessageCount().get()) {
                        // signal to main thread that all messages are received, application can exit
                        this.messagesReceivedLatch.countDown();
                        log.info("All messages received");
                }
            });
        return fut;
    }

    private Future<Void> deleteConsumer() {
        Future<Void> fut = Future.future();

        this.client.delete(this.consumer.getBaseUri())
            .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/vnd.kafka.v2+json")
            .as(BodyCodec.jsonObject())
            .send(ar -> {
                if (ar.succeeded()) {
                    HttpResponse<JsonObject> response = ar.result();
                    if (response.statusCode() == HttpResponseStatus.NO_CONTENT.code()) {
                        log.info("Consumer {} deleted", this.consumer.getInstanceId());
                        fut.complete();
                    } else {
                        fut.fail(new RuntimeException("Got HTTP status code " + response.statusCode()));
                    } 
                } else {
                    fut.fail(ar.cause());
                }
            });
        return fut;
    }

    /**
     * Information about using the consumer on the bridge
     */
    class CreatedConsumer {
    
        private final String instanceId;
        private final String baseUri;
        
        CreatedConsumer(String instanceId, String baseUri) {
            this.instanceId = instanceId;
            this.baseUri = baseUri;
        }

        /**
         * @return consumer instance-id/name
         */
        public String getInstanceId() {
            return instanceId;
        }

        /**
         * @return consumer URI to use for all next calls
         */
        public String getBaseUri() {
            return baseUri;
        }

        @Override
        public String toString() {
            return "CreatedConsumer(" +
                    "instanceId=" + this.instanceId +
                    ",baseUri=" + this.baseUri +
                    ")";
        }
    }

    /**
     * Represents a consumed record
     */
    class ConsumerRecord {

        private final String topic;
        private final Object key;
        private final Object value;
        private final int partition;
        private final long offset;

        ConsumerRecord(String topic, Object key, Object value, int partition, long offset) {
            this.topic = topic;
            this.key = key;
            this.value = value;
            this.partition = partition;
            this.offset = offset;
        }

        /**
         * @return topic from which the message was consumed
         */
        public String getTopic() {
            return topic;
        }

        /**
         * @return the message key
         */
        public Object getKey() {
            return key;
        }

        /**
         * @return the message value
         */
        public Object getValue() {
            return value;
        }

        /**
         * @return the topic partition from which the message was consumed
         */
        public int getPartition() {
            return partition;
        }

        /**
         * @return the message offset in the partition
         */
        public long getOffset() {
            return offset;
        }

        @Override
        public String toString() {
            return "ConsumerRecord(" +
                    "topic=" + this.topic +
                    ",key=" + this.key +
                    ",value=" + this.value +
                    ",partition=" + this.partition +
                    ",offset=" + this.offset +
                    ")";
        }
    }
}