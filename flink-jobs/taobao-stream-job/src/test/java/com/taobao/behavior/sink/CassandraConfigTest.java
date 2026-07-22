package com.taobao.behavior.sink;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CassandraConfigTest {
    @Test
    void acceptsAstraConfigurationWithoutDatabaseId() throws Exception {
        Path bundle = Files.createTempFile("secure-connect-", ".zip");
        CassandraConfig config = CassandraConfig.fromEnvironment(environment(bundle));

        assertEquals("astra", config.mode());
        assertEquals("taobao_streaming", config.keyspace());
        assertEquals("user_active_cart", config.table());
        assertEquals(bundle, config.secureBundlePath());
        assertEquals(10_000L, config.connectTimeout().toMillis());
        assertEquals(5_000L, config.requestTimeout().toMillis());
    }

    @Test
    void acceptsLocalHostsDatacenterAndOptionalCredentials() {
        CassandraConfig config = CassandraConfig.fromEnvironment(Map.of(
                "CASSANDRA_MODE", "local",
                "CASSANDRA_HOSTS", "cassandra, 127.0.0.1",
                "CASSANDRA_PORT", "9042",
                "CASSANDRA_DATACENTER", "datacenter1",
                "CASSANDRA_KEYSPACE", "taobao_streaming",
                "CASSANDRA_TABLE", "user_active_cart",
                "CASSANDRA_USERNAME", "local-user",
                "CASSANDRA_PASSWORD", "local-password",
                "CASSANDRA_CONNECT_TIMEOUT_MS", "12000",
                "CASSANDRA_REQUEST_TIMEOUT_MS", "6000"));

        assertEquals("local", config.mode());
        assertEquals(2, config.hosts().size());
        assertEquals(9042, config.port());
        assertEquals("datacenter1", config.datacenter());
        assertEquals("local-user", config.username());
        assertEquals(12_000L, config.connectTimeout().toMillis());
        assertEquals(6_000L, config.requestTimeout().toMillis());
    }

    @Test
    void rejectsMissingTokenWithoutLeakingAnySecret() throws Exception {
        Map<String, String> environment = environment(Files.createTempFile("secure-connect-", ".zip"));
        environment.remove("ASTRA_DB_APPLICATION_TOKEN");

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class, () -> CassandraConfig.fromEnvironment(environment));

        assertTrue(error.getMessage().contains("ASTRA_DB_APPLICATION_TOKEN"));
        assertTrue(!error.getMessage().contains("test-token"));
    }

    @Test
    void rejectsMissingOrInvalidSecureBundlePath() {
        Map<String, String> missing = environment(Path.of("missing-secure-connect.zip"));
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class, () -> CassandraConfig.fromEnvironment(missing));

        assertTrue(error.getMessage().contains("ASTRA_DB_SECURE_BUNDLE_PATH"));
    }

    @Test
    void rejectsUnsupportedProviderAndCqlIdentifiers() throws Exception {
        Map<String, String> provider = environment(Files.createTempFile("secure-connect-", ".zip"));
        provider.put("CASSANDRA_MODE", "other");
        assertThrows(IllegalArgumentException.class, () -> CassandraConfig.fromEnvironment(provider));

        Map<String, String> identifier = environment(Files.createTempFile("secure-connect-", ".zip"));
        identifier.put("CASSANDRA_TABLE", "activity; DROP TABLE x");
        assertThrows(IllegalArgumentException.class, () -> CassandraConfig.fromEnvironment(identifier));

        Map<String, String> table = environment(Files.createTempFile("secure-connect-", ".zip"));
        table.put("CASSANDRA_TABLE", "other_table");
        assertThrows(IllegalArgumentException.class, () -> CassandraConfig.fromEnvironment(table));

        Map<String, String> local = new HashMap<>();
        local.put("CASSANDRA_MODE", "local");
        local.put("CASSANDRA_HOSTS", "localhost");
        local.put("CASSANDRA_DATACENTER", "datacenter1");
        local.put("CASSANDRA_KEYSPACE", "taobao_streaming");
        local.put("CASSANDRA_TABLE", "user_active_cart");
        local.put("CASSANDRA_USERNAME", "user-only");
        assertThrows(IllegalArgumentException.class, () -> CassandraConfig.fromEnvironment(local));
    }

    private static Map<String, String> environment(Path bundle) {
        Map<String, String> values = new HashMap<>();
        values.put("CASSANDRA_MODE", "astra");
        values.put("CASSANDRA_KEYSPACE", "taobao_streaming");
        values.put("CASSANDRA_TABLE", "user_active_cart");
        values.put("ASTRA_DB_SECURE_BUNDLE_PATH", bundle.toString());
        values.put("ASTRA_DB_APPLICATION_TOKEN", "test-token");
        return values;
    }
}
