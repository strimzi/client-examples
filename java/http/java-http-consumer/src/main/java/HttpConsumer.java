/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import io.strimzi.common.TracingInitializer;
import io.strimzi.common.TracingSystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("classFanOutComplexity")
public class HttpConsumer {

    private static final Logger log = LogManager.getLogger(HttpConsumer.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String ENABLE_AUTO_COMMIT_CONFIG = "enable.auto.commit";

    private final HttpConsumerConfig config;
    private HttpClient httpClient;
    private URI createConsumerEndpoint;
    private URI consumerEndpoint;
    private int messageReceived = 0;
    private ScheduledExecutorService executorService;
    private Tracer tracer;
    private CountDownLatch messagesReceivedLatch;
    private boolean enableAutoCommit = false;

    private static List<String> commonProps;
    static {
        commonProps = new ArrayList<>();
        commonProps.add("auto.offset.reset");
        commonProps.add("enable.auto.commit");
        commonProps.add("fetch.min.bytes");
        commonProps.add("request.timeout.ms");
        commonProps.add("client.id");
        commonProps.add("isolation.level");
    }

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        HttpConsumerConfig config = HttpConsumerConfig.fromEnv();
        CountDownLatch messagesReceivedLatch = new CountDownLatch(1);

        TracingSystem tracingSystem = config.getTracingSystem();
        if (tracingSystem != TracingSystem.NONE) {
            if (tracingSystem == TracingSystem.OPENTELEMETRY) {
                TracingInitializer.otelInitialize();
            } else {
                log.error("Error: STRIMZI_TRACING_SYSTEM {} is not recognized or supported!", config.getTracingSystem());
            }
        }

        HttpConsumer consumer = new HttpConsumer(config, messagesReceivedLatch);

        try {
            consumer.createConsumer();
            consumer.subscribe();
            consumer.run();
            log.info("Waiting for receiving all messages");
            messagesReceivedLatch.await();
        } finally {
            consumer.deleteConsumer();
        }
    }

    public HttpConsumer(HttpConsumerConfig config, CountDownLatch messagesReceivedLatch) throws URISyntaxException {
        System.setProperty("otel.metrics.exporter", "none"); // disable metrics
        this.config = config;
        this.messagesReceivedLatch = messagesReceivedLatch;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.httpClient = HttpClient.newHttpClient();
        this.createConsumerEndpoint = new URI("http://" + this.config.getHostName() + ":" + this.config.getPort() + "/consumers/" + this.config.getGroupId());
        this.tracer = this.config.getTracingSystem().equals(TracingSystem.NONE) ?
                OpenTelemetry.noop().getTracer("client-examples") :
                GlobalOpenTelemetry.getTracer("client-examples");
    }

    public void createConsumer() throws IOException, InterruptedException, URISyntaxException {
        Properties props = config.getProperties();
        enableAutoCommit = (props.getProperty(ENABLE_AUTO_COMMIT_CONFIG)) == null || Boolean.parseBoolean(props.getProperty(ENABLE_AUTO_COMMIT_CONFIG));
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<>();
        map.put("name", "my-consumer");
        map.put("format", "json");
        for (String k : commonProps) {
            processProperty(props, k, map);
        }

        String consumerInfo = objectMapper.writeValueAsString(map);
        log.info("Creating consumer = {}", consumerInfo);
        this.consumerEndpoint = new URI("http://" + this.config.getHostName() + ":" + this.config.getPort() + "/consumers/" + this.config.getGroupId() + "/instances/" + this.config.getClientId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(this.createConsumerEndpoint)
                .headers("Content-Type", "application/vnd.kafka.v2+json")
                .POST(HttpRequest.BodyPublishers.ofString(consumerInfo))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == HttpResponseStatus.OK.code()) {
            log.info("Consumer successfully created {}", response.body());
            JsonNode json = MAPPER.readTree(response.body());
            URI baseURI = new URI(json.get("base_uri").asText());
            if (!this.consumerEndpoint.equals(baseURI)) {
                throw new RuntimeException(String.format("Expected consumer endpoint %s different from the returned base_uri %s", this.consumerEndpoint, baseURI));
            }
        } else {
            throw new RuntimeException(String.format("Failed to create consumer. Status code: %s, response: %s", response.statusCode(), response.body()));
        }
    }

    public void processProperty(Properties props, String k, Map<String, Object> map) {
        if (props.stringPropertyNames().contains(k)) {
            String v = props.getProperty(k);
            Object obj;
            if (isStringBoolean(v)) {
                obj = Boolean.parseBoolean(v);
            } else if (isStringNumber(v)) {
                obj = Integer.parseInt(v);
            } else {
                obj = v;
            }
            map.put(k, obj);
            log.info("key: {}, value: {}", k, v);
        }
    }

    public void subscribe() throws URISyntaxException, IOException, InterruptedException {
        URI subscriptionEndpoint = new URI(this.consumerEndpoint.toString() + "/subscription");
        String topics = "{\"topics\":[\"" + this.config.getTopic() + "\"]}";
        log.info("Subscribing consumer: {} to topics: {}", this.config.getClientId(), this.config.getTopic());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(subscriptionEndpoint)
                .headers("Content-Type", "application/vnd.kafka.v2+json")
                .POST(HttpRequest.BodyPublishers.ofString(topics))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == HttpResponseStatus.NO_CONTENT.code()) {
            log.info("Successfully subscribed to topics {}", this.config.getTopic());
        } else {
            throw new RuntimeException(String.format("Failed to subscribe consumer: %s to topics: %s, response: %s", this.config.getClientId(), this.config.getTopic(), response.body()));
        }
    }

    @SuppressWarnings({"Regexp"})
    public static boolean isStringBoolean(String input) {
        String lowerCaseInput = input.toLowerCase();
        return lowerCaseInput.equals("true") || lowerCaseInput.equals("false");
    }

    public static boolean isStringNumber(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void run() throws InterruptedException {
        log.info("Scheduling periodic poll every {} ms waiting for {} ...", this.config.getPollInterval(), this.config.getMessageCount());
        this.executorService.scheduleAtFixedRate(this::scheduledPoll, 0, this.config.getPollInterval(), TimeUnit.MILLISECONDS);
        log.info("... {} messages received", this.messageReceived);
    }

    private void scheduledPoll() {
        this.poll();
        if (this.config.getMessageCount() != null && this.messageReceived == this.config.getMessageCount()) {
            this.executorService.shutdown();
            this.messagesReceivedLatch.countDown();
            log.info("All messages received");
        }
    }

    public void poll() {
        try {
            log.info("Polling for records ...");
            URI recordsEndpoint = new URI(this.consumerEndpoint.toString() + "/records?timeout=" + this.config.getPollTimeout());

            SpanBuilder spanBuilder = this.tracer.spanBuilder("consume-messages");
            spanBuilder.setSpanKind(SpanKind.CLIENT);
            spanBuilder.setAttribute(SemanticAttributes.HTTP_METHOD, "GET");
            spanBuilder.setAttribute(SemanticAttributes.HTTP_URL, recordsEndpoint.toString());
            Span span = spanBuilder.startSpan();

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(recordsEndpoint)
                    .headers("Accept", "application/vnd.kafka.json.v2+json")
                    .GET();

            try (Scope ignored = span.makeCurrent()) {
                GlobalOpenTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), builder, HttpRequest.Builder::setHeader);
                HttpRequest request = builder.build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                ArrayNode records = (ArrayNode) MAPPER.readTree(response.body());
                log.info("... got {} records", records.size());

                for (JsonNode record : records) {
                    log.info("Record {}", record);
                    this.messageReceived++;
                }
                if (records.size() > 0 && !enableAutoCommit) {
                    this.commitMessages();
                }
                span.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, response.statusCode());
                span.setStatus(response.statusCode() == 200 ? StatusCode.OK : StatusCode.ERROR);
            } finally {
                span.end();
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void commitMessages() {
        try {
            log.info("Committing Messages ...");
            URI commitEndpoint = new URI(this.consumerEndpoint.toString() + "/offsets");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(commitEndpoint)
                    .headers("Content-Type", "application/vnd.kafka.v2+json")
                    .POST(HttpRequest.BodyPublishers.ofString("{}"))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HttpResponseStatus.NO_CONTENT.code()) {
                log.info("Successfully committed messages.");
            } else {
                throw new RuntimeException("Failed to commit messages. Response: " + response.body());
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteConsumer() throws IOException, InterruptedException {
        log.info("Deleting consumer = {}", this.config.getClientId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(this.consumerEndpoint)
                .DELETE()
                .build();
        HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());

        if (response.statusCode() == HttpResponseStatus.NO_CONTENT.code()) {
            log.info("Successfully deleted consumer {}", this.config.getClientId());
        } else {
            throw new RuntimeException(String.format("Failed to delete consumer: %s, response: %s", this.config.getClientId(), response.body()));
        }
    }
}
