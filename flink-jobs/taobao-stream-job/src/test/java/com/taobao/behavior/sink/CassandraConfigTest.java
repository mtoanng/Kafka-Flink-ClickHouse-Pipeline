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

        assertEquals("astra", config.provider());
        assertEquals("taobao_streaming", config.keyspace());
        assertEquals("user_active_cart", config.table());
        assertEquals(bundle, config.secureBundlePath());
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
        provider.put("CASSANDRA_PROVIDER", "other");
        assertThrows(IllegalArgumentException.class, () -> CassandraConfig.fromEnvironment(provider));

        Map<String, String> identifier = environment(Files.createTempFile("secure-connect-", ".zip"));
        identifier.put("CASSANDRA_TABLE", "activity; DROP TABLE x");
        assertThrows(IllegalArgumentException.class, () -> CassandraConfig.fromEnvironment(identifier));

        Map<String, String> table = environment(Files.createTempFile("secure-connect-", ".zip"));
        table.put("CASSANDRA_TABLE", "other_table");
        assertThrows(IllegalArgumentException.class, () -> CassandraConfig.fromEnvironment(table));
    }

    private static Map<String, String> environment(Path bundle) {
        Map<String, String> values = new HashMap<>();
        values.put("CASSANDRA_PROVIDER", "astra");
        values.put("CASSANDRA_KEYSPACE", "taobao_streaming");
        values.put("CASSANDRA_TABLE", "user_active_cart");
        values.put("ASTRA_DB_SECURE_BUNDLE_PATH", bundle.toString());
        values.put("ASTRA_DB_APPLICATION_TOKEN", "test-token");
        return values;
    }
}
