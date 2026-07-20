package com.taobao.behavior;

import java.util.Map;
import java.util.Properties;

final class RuntimeProfileConfig {
    private final Map<String, String> environment;
    private final boolean servingEnabled;
    private final boolean cdcEnabled;
    private final boolean observabilityEnabled;

    private RuntimeProfileConfig(Map<String, String> environment) {
        this.environment = environment;
        servingEnabled = booleanValue("SERVING_ENABLED", false);
        cdcEnabled = booleanValue("CDC_ENABLED", false);
        observabilityEnabled = booleanValue("OBSERVABILITY_ENABLED", false);
        validateOptionalProfiles();
        kafkaProperties();
    }

    static RuntimeProfileConfig fromEnvironment(Map<String, String> environment) {
        return new RuntimeProfileConfig(environment);
    }

    boolean isServingEnabled() {
        return servingEnabled;
    }

    boolean isCdcEnabled() {
        return cdcEnabled;
    }

    boolean isObservabilityEnabled() {
        return observabilityEnabled;
    }

    String value(String key, String defaultValue) {
        String value = environment.get(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    Properties kafkaProperties() {
        String protocol = value("KAFKA_SECURITY_PROTOCOL", "PLAINTEXT").toUpperCase();
        if (!"PLAINTEXT".equals(protocol) && !"SASL_SSL".equals(protocol)) {
            throw new IllegalArgumentException(
                    "KAFKA_SECURITY_PROTOCOL must be PLAINTEXT or SASL_SSL");
        }
        Properties properties = new Properties();
        properties.setProperty("security.protocol", protocol);
        String mechanism = optional("KAFKA_SASL_MECHANISM");
        String username = optional("KAFKA_SASL_USERNAME");
        String password = optional("KAFKA_SASL_PASSWORD");
        String jaas = optional("KAFKA_SASL_JAAS_CONFIG");
        if ("PLAINTEXT".equals(protocol)) {
            if (mechanism != null || username != null || password != null || jaas != null) {
                throw new IllegalArgumentException(
                        "SASL settings require KAFKA_SECURITY_PROTOCOL=SASL_SSL");
            }
            return properties;
        }
        if (mechanism == null) {
            throw new IllegalArgumentException("KAFKA_SASL_MECHANISM is required for SASL_SSL");
        }
        if ((username == null) != (password == null)) {
            throw new IllegalArgumentException(
                    "KAFKA_SASL_USERNAME and KAFKA_SASL_PASSWORD must be provided together");
        }
        if (jaas == null && username == null) {
            throw new IllegalArgumentException(
                    "SASL_SSL requires KAFKA_SASL_JAAS_CONFIG or username/password");
        }
        properties.setProperty("sasl.mechanism", mechanism);
        if (jaas != null) {
            properties.setProperty("sasl.jaas.config", jaas);
        } else {
            properties.setProperty(
                    "sasl.jaas.config",
                    "org.apache.kafka.common.security.plain.PlainLoginModule required username=\""
                            + username + "\" password=\"" + password + "\";");
        }
        return properties;
    }

    private void validateOptionalProfiles() {
        if (servingEnabled) {
            required("SCYLLA_HOST");
            required("SCYLLA_LOCAL_DATACENTER");
            optionalPair("SCYLLA_USER", "SCYLLA_PASSWORD");
        }
        if (cdcEnabled) {
            required("RULES_KAFKA_TOPIC");
            required("POSTGRES_HOST");
            required("POSTGRES_DB");
            required("POSTGRES_USER");
            required("POSTGRES_PASSWORD");
            required("CONNECT_URL");
        }
        if (observabilityEnabled) {
            required("GRAFANA_ENDPOINT");
        }
    }

    private boolean booleanValue(String key, boolean defaultValue) {
        String value = optional(key);
        if (value == null) {
            return defaultValue;
        }
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        throw new IllegalArgumentException(key + " must be true or false");
    }

    private String required(String key) {
        String value = optional(key);
        if (value == null) {
            throw new IllegalArgumentException(key + " is required when its profile is enabled");
        }
        return value;
    }

    private void optionalPair(String first, String second) {
        if ((optional(first) == null) != (optional(second) == null)) {
            throw new IllegalArgumentException(first + " and " + second + " must be provided together");
        }
    }

    private String optional(String key) {
        String value = environment.get(key);
        return value == null || value.isBlank() ? null : value;
    }
}
