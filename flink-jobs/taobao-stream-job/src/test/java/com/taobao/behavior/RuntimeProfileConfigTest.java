package com.taobao.behavior;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class RuntimeProfileConfigTest {
    @Test
    void coreDoesNotRequireCassandraOrCdcValues() {
        RuntimeProfileConfig config = RuntimeProfileConfig.fromEnvironment(Map.of());

        assertFalse(config.isCassandraEnabled());
        assertFalse(config.isCdcEnabled());
        assertFalse(config.isObservabilityEnabled());
        assertEquals("PLAINTEXT", config.kafkaProperties().getProperty("security.protocol"));
    }

    @Test
    void cassandraProfileRequiresOnlyCassandraConfiguration() throws Exception {
        java.nio.file.Path secureBundle = java.nio.file.Files.createTempFile("secure-connect-", ".zip");
        RuntimeProfileConfig config = RuntimeProfileConfig.fromEnvironment(
                Map.of(
                        "CASSANDRA_ENABLED", "true",
                        "CASSANDRA_PROVIDER", "astra",
                        "CASSANDRA_KEYSPACE", "taobao_streaming",
                        "CASSANDRA_TABLE", "user_active_cart",
                        "ASTRA_DB_SECURE_BUNDLE_PATH", secureBundle.toString(),
                        "ASTRA_DB_APPLICATION_TOKEN", "test-token"));

        assertTrue(config.isCassandraEnabled());
        assertFalse(config.isCdcEnabled());
    }

    @Test
    void cdcProfileRequiresItsOwnConfiguration() {
        RuntimeProfileConfig config = RuntimeProfileConfig.fromEnvironment(cdcEnvironment());

        assertTrue(config.isCdcEnabled());
        assertFalse(config.isCassandraEnabled());
    }

    @Test
    void partialOptionalConfigurationIsRejectedOnlyWhenEnabled() {
        assertThrows(
                IllegalArgumentException.class,
                () -> RuntimeProfileConfig.fromEnvironment(Map.of("CASSANDRA_ENABLED", "true")));
        Map<String, String> cdc = cdcEnvironment();
        cdc.remove("CONNECT_URL");
        assertThrows(IllegalArgumentException.class, () -> RuntimeProfileConfig.fromEnvironment(cdc));
        RuntimeProfileConfig.fromEnvironment(Map.of("ASTRA_DB_APPLICATION_TOKEN", "ignored-when-disabled"));
    }

    @Test
    void observabilityConfigurationIsIgnoredUntilItsProfileIsEnabled() {
        RuntimeProfileConfig.fromEnvironment(Map.of("GRAFANA_ENDPOINT", "ignored-when-disabled"));
        assertThrows(
                IllegalArgumentException.class,
                () -> RuntimeProfileConfig.fromEnvironment(Map.of("OBSERVABILITY_ENABLED", "true")));
        RuntimeProfileConfig.fromEnvironment(
                Map.of("OBSERVABILITY_ENABLED", "true", "GRAFANA_ENDPOINT", "http://grafana:3000"));
    }

    @Test
    void managedSaslSslMapsToKafkaClientProperties() {
        Map<String, String> environment = new HashMap<>();
        environment.put("KAFKA_SECURITY_PROTOCOL", "SASL_SSL");
        environment.put("KAFKA_SASL_MECHANISM", "PLAIN");
        environment.put("KAFKA_SASL_USERNAME", "key");
        environment.put("KAFKA_SASL_PASSWORD", "secret");

        Properties properties = RuntimeProfileConfig.fromEnvironment(environment).kafkaProperties();

        assertEquals("SASL_SSL", properties.getProperty("security.protocol"));
        assertEquals("PLAIN", properties.getProperty("sasl.mechanism"));
        assertTrue(properties.getProperty("sasl.jaas.config").contains("username=\"key\""));
    }

    @Test
    void partialSaslConfigurationIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> RuntimeProfileConfig.fromEnvironment(
                        Map.of("KAFKA_SECURITY_PROTOCOL", "SASL_SSL")));
    }

    private static Map<String, String> cdcEnvironment() {
        return new HashMap<>(Map.of(
                "CDC_ENABLED", "true",
                "RULES_KAFKA_TOPIC", "behavior-rules",
                "POSTGRES_HOST", "postgres.internal",
                "POSTGRES_DB", "taobao_behavior",
                "POSTGRES_USER", "connector",
                "POSTGRES_PASSWORD", "secret",
                "CONNECT_URL", "http://connect.internal:8083"));
    }
}
