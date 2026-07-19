package com.taobao.behavior;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import java.net.InetSocketAddress;
import java.util.regex.Pattern;

public final class ScyllaLookupCli {
    private static final Pattern CQL_IDENTIFIER = Pattern.compile("[a-z][a-z0-9_]*");
    private ScyllaLookupCli() {}

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("usage: ScyllaLookupCli <positive-user-id>");
        }
        long userId = parseUserId(args[0]);
        String host = environment("SCYLLA_HOST", "localhost");
        int port = Integer.parseInt(environment("SCYLLA_PORT", "9042"));
        String localDatacenter = environment("SCYLLA_LOCAL_DATACENTER", "datacenter1");
        String user = environment("SCYLLA_USER", "");
        String password = environment("SCYLLA_PASSWORD", "");
        String keyspace = validIdentifier(environment("SCYLLA_KEYSPACE", "taobao_behavior"));

        CqlSessionBuilder builder =
                CqlSession.builder()
                        .addContactPoint(new InetSocketAddress(host, port))
                        .withLocalDatacenter(localDatacenter);
        if (!user.isBlank()) {
            builder.withAuthCredentials(user, password);
        }
        try (CqlSession session = builder.build()) {
            PreparedStatement query =
                    session.prepare(
                            "SELECT user_id, last_event_time, last_behavior_type, last_item_id, "
                                    + "last_category_id, session_event_count, updated_at FROM "
                                    + keyspace
                                    + ".user_current_activity WHERE user_id = ?");
            Row row = session.execute(query.bind(userId)).one();
            if (row == null) {
                System.out.println("NOT FOUND user_id=" + userId);
                return;
            }
            System.out.printf(
                    "user_id=%d last_event_time=%s last_behavior_type=%s last_item_id=%d "
                            + "last_category_id=%d session_event_count=%d updated_at=%s%n",
                    row.getLong("user_id"),
                    row.getInstant("last_event_time"),
                    row.getString("last_behavior_type"),
                    row.getLong("last_item_id"),
                    row.getLong("last_category_id"),
                    row.getLong("session_event_count"),
                    row.getInstant("updated_at"));
        }
    }

    static long parseUserId(String value) {
        long userId = Long.parseLong(value);
        if (userId <= 0) {
            throw new IllegalArgumentException("user ID must be positive");
        }
        return userId;
    }

    private static String validIdentifier(String value) {
        if (!CQL_IDENTIFIER.matcher(value).matches()) {
            throw new IllegalArgumentException("SCYLLA_KEYSPACE must be a lowercase CQL identifier");
        }
        return value;
    }

    private static String environment(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
