package com.taobao.behavior.sink;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class CassandraConfig {
    private static final Pattern CQL_IDENTIFIER = Pattern.compile("[a-z][a-z0-9_]*");

    private final String mode;
    private final List<String> hosts;
    private final int port;
    private final String datacenter;
    private final String keyspace;
    private final String table;
    private final String username;
    private final String password;
    private final Path secureBundlePath;
    private final String applicationToken;
    private final Duration connectTimeout;
    private final Duration requestTimeout;

    private CassandraConfig(
            String mode,
            List<String> hosts,
            int port,
            String datacenter,
            String keyspace,
            String table,
            String username,
            String password,
            Path secureBundlePath,
            String applicationToken,
            Duration connectTimeout,
            Duration requestTimeout) {
        this.mode = mode;
        this.hosts = hosts;
        this.port = port;
        this.datacenter = datacenter;
        this.keyspace = keyspace;
        this.table = table;
        this.username = username;
        this.password = password;
        this.secureBundlePath = secureBundlePath;
        this.applicationToken = applicationToken;
        this.connectTimeout = connectTimeout;
        this.requestTimeout = requestTimeout;
    }

    public static CassandraConfig fromEnvironment(Map<String, String> environment) {
        String mode = required(environment, "CASSANDRA_MODE", "Cassandra runtime").toLowerCase();
        if (!"local".equals(mode) && !"astra".equals(mode)) {
            throw new IllegalArgumentException("CASSANDRA_MODE must be local or astra");
        }
        String keyspace = identifier(
                required(environment, "CASSANDRA_KEYSPACE", mode), "CASSANDRA_KEYSPACE");
        String table = identifier(
                required(environment, "CASSANDRA_TABLE", mode), "CASSANDRA_TABLE");
        if (!"user_active_cart".equals(table)) {
            throw new IllegalArgumentException("CASSANDRA_TABLE must be user_active_cart");
        }
        Duration connectTimeout = Duration.ofMillis(
                positiveLong(environment, "CASSANDRA_CONNECT_TIMEOUT_MS", 10_000L));
        Duration requestTimeout = Duration.ofMillis(
                positiveLong(environment, "CASSANDRA_REQUEST_TIMEOUT_MS", 5_000L));

        if ("local".equals(mode)) {
            List<String> hosts = Arrays.stream(
                            required(environment, "CASSANDRA_HOSTS", mode).split(","))
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .collect(Collectors.toList());
            if (hosts.isEmpty()) {
                throw new IllegalArgumentException("CASSANDRA_HOSTS must contain at least one host");
            }
            int port = positivePort(environment.getOrDefault("CASSANDRA_PORT", "9042"));
            String datacenter = required(environment, "CASSANDRA_DATACENTER", mode);
            String username = optional(environment, "CASSANDRA_USERNAME");
            String password = optional(environment, "CASSANDRA_PASSWORD");
            if ((username == null) != (password == null)) {
                throw new IllegalArgumentException(
                        "CASSANDRA_USERNAME and CASSANDRA_PASSWORD must be provided together");
            }
            return new CassandraConfig(
                    mode,
                    hosts,
                    port,
                    datacenter,
                    keyspace,
                    table,
                    username,
                    password,
                    null,
                    null,
                    connectTimeout,
                    requestTimeout);
        }

        Path secureBundlePath = Path.of(
                required(environment, "ASTRA_DB_SECURE_BUNDLE_PATH", mode));
        if (!Files.isRegularFile(secureBundlePath)) {
            throw new IllegalArgumentException(
                    "ASTRA_DB_SECURE_BUNDLE_PATH must reference a readable file");
        }
        return new CassandraConfig(
                mode,
                List.of(),
                0,
                null,
                keyspace,
                table,
                null,
                null,
                secureBundlePath,
                required(environment, "ASTRA_DB_APPLICATION_TOKEN", mode),
                connectTimeout,
                requestTimeout);
    }

    public String mode() { return mode; }
    public List<String> hosts() { return hosts; }
    public int port() { return port; }
    public String datacenter() { return datacenter; }
    public String keyspace() { return keyspace; }
    public String table() { return table; }
    public String username() { return username; }
    public String password() { return password; }
    public Path secureBundlePath() { return secureBundlePath; }
    public String applicationToken() { return applicationToken; }
    public Duration connectTimeout() { return connectTimeout; }
    public Duration requestTimeout() { return requestTimeout; }

    private static String required(
            Map<String, String> environment, String key, String profile) {
        String value = optional(environment, key);
        if (value == null) {
            throw new IllegalArgumentException(key + " is required for " + profile);
        }
        return value;
    }

    private static String optional(Map<String, String> environment, String key) {
        String value = environment.get(key);
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static long positiveLong(
            Map<String, String> environment, String key, long defaultValue) {
        String raw = environment.getOrDefault(key, Long.toString(defaultValue));
        try {
            long value = Long.parseLong(raw);
            if (value <= 0) {
                throw new NumberFormatException("not positive");
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(key + " must be a positive integer", exception);
        }
    }

    private static int positivePort(String raw) {
        try {
            int value = Integer.parseInt(raw);
            if (value < 1 || value > 65_535) {
                throw new NumberFormatException("outside port range");
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "CASSANDRA_PORT must be an integer between 1 and 65535", exception);
        }
    }

    private static String identifier(String value, String key) {
        if (!CQL_IDENTIFIER.matcher(value).matches()) {
            throw new IllegalArgumentException(key + " must be a lowercase CQL identifier");
        }
        return value;
    }
}
