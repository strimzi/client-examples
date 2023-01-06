/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaClientSupplier;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Properties;

public class KafkaStreamsExample {
    private static final Logger log = LogManager.getLogger(KafkaStreamsExample.class);

    public static void main(String[] args) {
        KafkaStreamsConfig config = KafkaStreamsConfig.fromEnv();

        log.info(KafkaStreamsConfig.class.getName() + ": {}",  config.toString());

        Properties props = KafkaStreamsConfig.createProperties(config);

        StreamsBuilder builder = new StreamsBuilder();

        builder.stream(config.getSourceTopic(), Consumed.with(Serdes.String(), Serdes.String()))
                .mapValues(value -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append(value);
                    return sb.reverse().toString();
                })
                .to(config.getTargetTopic(), Produced.with(Serdes.String(), Serdes.String()));

        KafkaStreams streams;

        TracingSystem tracingSystem = config.getTracingSystem();
        if (tracingSystem != TracingSystem.NONE) {
            if (tracingSystem == TracingSystem.OPENTELEMETRY) {
                props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
                props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

                KafkaClientSupplier supplier = new TracingKafkaClientSupplier();
                streams = new KafkaStreams(builder.build(), props, supplier);
            } else {
                throw new RuntimeException("Error: TRACING_SYSTEM " + tracingSystem + " is not recognized or supported!");
            }
        } else {
            streams = new KafkaStreams(builder.build(), props);
        }

        streams.start();
    }
}