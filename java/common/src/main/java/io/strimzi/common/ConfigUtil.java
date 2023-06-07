/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.common;

import java.util.Locale;

/**
 * Provides utility methods for managing common configuration properties.
 */
public class ConfigUtil {
    /**
     * Converts environment variables into a corresponding property key format.
     *
     * @param envVar Name of the environment variable to be converted to property key format.
     * @return Returns a String which removes a prefix containing '_', converts to lower case and replaces '_' with '.'.
     */
    public static String convertEnvVarToPropertyKey(String envVar) {
        return envVar.substring(envVar.indexOf("_") + 1).toLowerCase(Locale.ENGLISH).replace("_", ".");
    }
}
