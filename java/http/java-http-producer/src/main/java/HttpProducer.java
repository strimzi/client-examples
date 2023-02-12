/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HttpProducer {

    private static final Logger log = LogManager.getLogger(HttpProducer.class);

    private final HttpProducerConfig config;
    private int messageSent = 0;
    private ScheduledExecutorService executorService;
    private HttpClient httpClient;
    private URI sendEndpoint;
    private Tracer tracer;

    public static void main(String[] args) throws InterruptedException, URISyntaxException {
        HttpProducerConfig config = HttpProducerConfig.fromEnv();

        TracingSystem tracingSystem = config.getTracingSystem();
        if (tracingSystem != TracingSystem.NONE) {
            if (tracingSystem == TracingSystem.OPENTELEMETRY) {
                TracingInitializer.otelInitialize();
            } else {
                log.error("Error: STRIMZI_TRACING_SYSTEM {} is not recognized or supported!", config.getTracingSystem());
            }
        }

        HttpProducer httpProducer = new HttpProducer(config);
        httpProducer.run();
    }

    public HttpProducer(HttpProducerConfig config) throws URISyntaxException {
        this.config = config;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.httpClient = HttpClient.newHttpClient();
        this.sendEndpoint = new URI("http://" + this.config.getHostName() + ":" + this.config.getPort() + "/topics/" + this.config.getTopic());
        this.tracer = this.config.getTracingSystem().equals(TracingSystem.NONE) ?
                OpenTelemetry.noop().getTracer("client-examples") :
                GlobalOpenTelemetry.getTracer("client-examples");
    }

    public void run() throws InterruptedException {
        log.info("Scheduling periodic send: {} messages every {} ms ...", this.config.getMessageCount(), this.config.getDelay());
        this.executorService.schedule(this::scheduledSend, this.config.getDelay(), TimeUnit.MILLISECONDS);
        this.executorService.awaitTermination(this.config.getDelay() * this.config.getMessageCount() + 60_000L, TimeUnit.MILLISECONDS);
        log.info("... {} messages sent", this.messageSent);
    }

    private void scheduledSend() {
        this.send();
        if (this.messageSent < this.config.getMessageCount()) {
            this.executorService.schedule(this::scheduledSend, this.config.getDelay(), TimeUnit.MILLISECONDS);
        } else {
            this.executorService.shutdown();
        }
    }

    private void send() {
        try {
            SpanBuilder spanBuilder = this.tracer.spanBuilder("send-messages");
            spanBuilder.setSpanKind(SpanKind.CLIENT);
            spanBuilder.setAttribute(SemanticAttributes.HTTP_METHOD, "POST");
            spanBuilder.setAttribute(SemanticAttributes.HTTP_URL, this.sendEndpoint.toString());
            Span span = spanBuilder.startSpan();

            String record = "{\"key\":\"key-" + this.messageSent + "\",\"value\":\"" + this.config.getMessage() + "-" + this.messageSent + "\"}";
            String records = "{\"records\":[" + record + "]}";
            log.info("Sending message = {}", record);

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(this.sendEndpoint)
                    .headers("Content-Type", "application/vnd.kafka.json.v2+json")
                    .POST(HttpRequest.BodyPublishers.ofString(records));

            try (Scope ignored = span.makeCurrent()) {
                GlobalOpenTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), builder, HttpRequest.Builder::setHeader);
                HttpRequest request = builder.build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                span.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, response.statusCode());
                span.setStatus(response.statusCode() == 200 ? StatusCode.OK : StatusCode.ERROR);

                log.info("code = {}, metadata = {}", response.statusCode(), response.body());
            } finally {
                span.end();
            }

            this.messageSent++;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
