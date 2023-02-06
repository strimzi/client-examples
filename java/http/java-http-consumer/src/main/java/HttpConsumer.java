/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.netty.handler.codec.http.HttpResponseStatus;
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

public class HttpConsumer {

    private static final Logger log = LogManager.getLogger(HttpConsumer.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final HttpConsumerConfig config;
    private HttpClient httpClient;
    private URI createConsumerEndpoint;
    private URI consumerEndpoint;
    private int messageReceived = 0;
    private ScheduledExecutorService executorService;


    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        HttpConsumerConfig config = HttpConsumerConfig.fromEnv();
        HttpConsumer consumer = new HttpConsumer(config);

        try {
            consumer.createConsumer();
            consumer.subscribe();
            consumer.run();
        } finally {
            consumer.deleteConsumer();
        }
    }

    public HttpConsumer(HttpConsumerConfig config) throws URISyntaxException {
        this.config = config;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.httpClient = HttpClient.newHttpClient();
        this.createConsumerEndpoint = new URI("http://" + this.config.getHostName() + ":" + this.config.getPort() + "/consumers/" + this.config.getGroupId());
    }

    public void createConsumer() throws IOException, InterruptedException, URISyntaxException {
        String consumerInfo = "{\"name\":\"" + this.config.getClientId() + "\",\"format\":\"json\",\"auto.offset.reset\":\"earliest\"}";
        log.info("Creating consumer = {}", consumerInfo);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(this.createConsumerEndpoint)
                .headers("Content-Type", "application/vnd.kafka.v2+json")
                .POST(HttpRequest.BodyPublishers.ofString(consumerInfo))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == HttpResponseStatus.OK.code()) {
            log.info("Consumer successfully created {}", response.body());
            JsonNode json = MAPPER.readTree(response.body());
            this.consumerEndpoint = new URI(json.get("base_uri").asText());
        } else {
            throw new RuntimeException(String.format("Failed to create consumer. Status code: %s, response: %s", response.statusCode(), response.body()));
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

    public void run() throws InterruptedException {
        log.info("Scheduling periodic poll every {} ms waiting for {} ...", this.config.getPollInterval(), this.config.getMessageCount());
        this.executorService.schedule(this::scheduledPoll, this.config.getPollInterval(), TimeUnit.MILLISECONDS);
        this.executorService.awaitTermination(this.config.getPollInterval() * this.config.getMessageCount() + 60_000L, TimeUnit.MILLISECONDS);
        log.info("... {} messages received", this.messageReceived);
    }

    private void scheduledPoll() {
        this.poll();
        if (this.messageReceived < this.config.getMessageCount()) {
            this.executorService.schedule(this::scheduledPoll, this.config.getPollInterval(), TimeUnit.MILLISECONDS);
        } else {
            this.executorService.shutdown();
        }
    }

    public void poll() {
        try {
            log.info("Polling for records ...");
            URI recordsEndpoint = new URI(this.consumerEndpoint.toString() + "/records?timeout=" + this.config.getPollTimeout());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(recordsEndpoint)
                    .headers("Accept", "application/vnd.kafka.json.v2+json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            ArrayNode records = (ArrayNode) MAPPER.readTree(response.body());
            log.info("... got {} records", records.size());

            for (JsonNode record : records) {
                log.info("Record {}", record);
                this.messageReceived++;
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
