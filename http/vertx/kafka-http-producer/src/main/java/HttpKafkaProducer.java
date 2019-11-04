import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * HttpKafkaProducer
 */
public class HttpKafkaProducer extends AbstractVerticle {

    private final static Logger log = LoggerFactory.getLogger(HttpKafkaProducer.class);

    private final HttpKafkaProducerConfig config;

    private WebClient client;
    private long sendTimer;
    private int messagesSent;

    /**
     * Constructor
     * 
     * @param config configuration
     */
    public HttpKafkaProducer(HttpKafkaProducerConfig config) {
        this.config = config;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        log.info("HTTP Kafka producer starting with config {}", this.config);

        WebClientOptions options = new WebClientOptions()
                .setDefaultHost(this.config.getHostname())
                .setDefaultPort(this.config.getPort());
        this.client = WebClient.create(vertx, options);

        this.sendTimer = vertx.setPeriodic(this.config.getSendInterval(), t -> {
            this.send(this.config.getTopic()).setHandler(ar -> {
                if (ar.succeeded()) {
                    log.info("Sent {}", ar.result());
                }
            });
        });
        startFuture.complete();
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        log.info("HTTP Kafka producer stopping");
        this.vertx.cancelTimer(this.sendTimer);
        stopFuture.complete();
    }

    private Future <List<OffsetRecordSent>> send(String topic) {
        Future<List<OffsetRecordSent>> fut = Future.future();        

        JsonObject records = new JsonObject();
        records.put("records", new JsonArray().add(new JsonObject().put("value", "message-" + this.messagesSent++)));

        this.client.post(this.config.getEndpointPrefix() + "/topics/" + topic)
            .putHeader(HttpHeaderNames.CONTENT_LENGTH.toString(), String.valueOf(records.toBuffer().length()))
            .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/vnd.kafka.json.v2+json")
            .as(BodyCodec.jsonObject())
            .sendJsonObject(records, ar -> {
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
                        this.vertx.close(done -> System.exit(0));
                }
            });
        return fut;
    }

    /**
     * Represents information about a message sent
     */
    class OffsetRecordSent {

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