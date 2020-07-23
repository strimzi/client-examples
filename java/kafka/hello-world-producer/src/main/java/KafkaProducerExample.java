/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import io.jaegertracing.Configuration;
import io.opentracing.Tracer;
import io.opentracing.contrib.kafka.TracingProducerInterceptor;
import io.opentracing.util.GlobalTracer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

public class KafkaProducerExample {
    private static final Logger log = LogManager.getLogger(KafkaProducerExample.class);

    public static void main(String[] args) throws InterruptedException {
        KafkaProducerConfig config = KafkaProducerConfig.fromEnv();
        Properties props = KafkaProducerConfig.createProperties(config);
        List<Header> headers = null;

        if (System.getenv("JAEGER_SERVICE_NAME") != null)   {
            Tracer tracer = Configuration.fromEnv().getTracer();
            GlobalTracer.registerIfAbsent(tracer);

            props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingProducerInterceptor.class.getName());
        }

        if (config.getHeaders() != null) {
            headers = new ArrayList<>();

            String[] headersArray = config.getHeaders().split(", [\t\n\r]");
            for (String header : headersArray) {
                headers.add(new RecordHeader(header.split("=")[0], header.split("=")[1].getBytes()));
            }
        }

        KafkaProducer producer = new KafkaProducer(props);
        log.info("Sending {} messages ...", config.getMessageCount());
        for (long i = 0; i < config.getMessageCount(); i++) {
            log.info("Sending messages \"" + config.getMessage() + " - {}\"{}", i, config.getHeaders() == null ? "" : " - with headers - " + config.getHeaders());
            producer.send(new ProducerRecord(config.getTopic(), null,null, null, "\"" + config.getMessage()  + " - " + i + "\"", headers));
            Thread.sleep(config.getDelay());
        }
        log.info("{} messages sent ...", config.getMessageCount());
        producer.close();
    }
}
