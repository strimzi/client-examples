/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

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

    public static void main(String[] args) throws InterruptedException, URISyntaxException {
        HttpProducerConfig config = HttpProducerConfig.fromEnv();
        HttpProducer httpProducer = new HttpProducer(config);
        httpProducer.run();
    }

    public HttpProducer(HttpProducerConfig config) throws URISyntaxException {
        this.config = config;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.httpClient = HttpClient.newHttpClient();
        this.sendEndpoint = new URI("http://" + this.config.getHostName() + ":" + this.config.getPort() + "/topics/" + this.config.getTopic());
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
            String record = "{\"key\":\"key-" + this.messageSent + "\",\"value\":\"" + this.config.getMessage() + "-" + this.messageSent + "\"}";
            String records = "{\"records\":[" + record + "]}";
            log.info("Sending message = {}", record);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(this.sendEndpoint)
                    .headers("Content-Type", "application/vnd.kafka.json.v2+json")
                    .POST(HttpRequest.BodyPublishers.ofString(records))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("code = {}, metadata = {}", response.statusCode(), response.body());

            this.messageSent++;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
