/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common;

import java.util.Locale;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.Map;


/**
 * Provides utility methods for managing common configuration properties.
 */
public class ConfigUtil {
    private static final String KAFKA_PREFIX = "KAFKA_";

    /**
     * Converts environment variables into a corresponding property key format.
     *
     * @param envVar Name of the environment variable to be converted to property key format.
     * @return Returns a String which removes a prefix containing '_', converts to lower case and replaces '_' with '.'.
     */
    public static String convertEnvVarToPropertyKey(String envVar) {
        return envVar.substring(envVar.indexOf("_") + 1).toLowerCase(Locale.ENGLISH).replace("_", ".");
    }

    /**
     * Retrieves Kafka-related properties from environment variables.
     * This method scans all environment variables, filters those that start with the prefix "KAFKA_",
     * converts them to a property key format, and collects them into a Properties object.
     *
     * @return Properties object containing Kafka-related properties derived from environment variables.
     */
    public static Properties getKafkaPropertiesFromEnv() {
        Properties properties = new Properties();
        properties.putAll(System.getenv()
                .entrySet()
                .stream()
                .filter(mapEntry -> mapEntry.getKey().startsWith(KAFKA_PREFIX))
                .collect(Collectors.toMap(mapEntry -> convertEnvVarToPropertyKey(mapEntry.getKey()), Map.Entry::getValue)));
        return properties;
    }
}
