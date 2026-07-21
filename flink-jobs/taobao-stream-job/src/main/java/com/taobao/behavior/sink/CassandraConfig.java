package com.taobao.behavior.sink;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Pattern;

public final class CassandraConfig {
    private static final Pattern CQL_IDENTIFIER = Pattern.compile("[a-z][a-z0-9_]*");

    private final String provider;
    private final String keyspace;
    private final String table;
    private final Path secureBundlePath;
    private final String applicationToken;

    private CassandraConfig(
            String provider,
            String keyspace,
            String table,
            Path secureBundlePath,
            String applicationToken) {
        this.provider = provider;
        this.keyspace = keyspace;
        this.table = table;
        this.secureBundlePath = secureBundlePath;
        this.applicationToken = applicationToken;
    }

    public static CassandraConfig fromEnvironment(Map<String, String> environment) {
        String provider = required(environment, "CASSANDRA_PROVIDER");
        if (!"astra".equals(provider)) {
            throw new IllegalArgumentException("CASSANDRA_PROVIDER must be astra");
        }
        String keyspace = identifier(required(environment, "CASSANDRA_KEYSPACE"), "CASSANDRA_KEYSPACE");
        String table = identifier(required(environment, "CASSANDRA_TABLE"), "CASSANDRA_TABLE");
        if (!"user_active_cart".equals(table)) {
            throw new IllegalArgumentException("CASSANDRA_TABLE must be user_active_cart");
        }
        Path secureBundlePath = Path.of(required(environment, "ASTRA_DB_SECURE_BUNDLE_PATH"));
        if (!Files.isRegularFile(secureBundlePath)) {
            throw new IllegalArgumentException("ASTRA_DB_SECURE_BUNDLE_PATH must reference a readable file");
        }
        return new CassandraConfig(
                provider,
                keyspace,
                table,
                secureBundlePath,
                required(environment, "ASTRA_DB_APPLICATION_TOKEN"));
    }

    public String provider() {
        return provider;
    }

    public String keyspace() {
        return keyspace;
    }

    public String table() {
        return table;
    }

    public Path secureBundlePath() {
        return secureBundlePath;
    }

    public String applicationToken() {
        return applicationToken;
    }

    private static String required(Map<String, String> environment, String key) {
        String value = environment.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(key + " is required when CASSANDRA_ENABLED=true");
        }
        return value;
    }

    private static String identifier(String value, String key) {
        if (!CQL_IDENTIFIER.matcher(value).matches()) {
            throw new IllegalArgumentException(key + " must be a lowercase CQL identifier");
        }
        return value;
    }
}
