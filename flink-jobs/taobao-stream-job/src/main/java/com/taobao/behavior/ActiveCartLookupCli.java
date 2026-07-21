package com.taobao.behavior;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import com.taobao.behavior.sink.CassandraConfig;
import com.taobao.behavior.sink.CassandraSessionFactory;

public final class ActiveCartLookupCli {
    private ActiveCartLookupCli() {}

    public static void main(String[] args) {
        long userId = parseUserId(args);
        CassandraConfig config = CassandraConfig.fromEnvironment(System.getenv());
        try (CqlSession session = new CassandraSessionFactory().open(config)) {
            PreparedStatement query = session.prepare(selectCql(config.keyspace(), config.table()));
            boolean found = false;
            for (Row row : session.execute(query.bind(userId))) {
                found = true;
                System.out.printf(
                        "user_id=%d item_id=%d category_id=%d added_at=%s last_updated_at=%s%n",
                        row.getLong("user_id"),
                        row.getLong("item_id"),
                        row.getLong("category_id"),
                        row.getInstant("added_at"),
                        row.getInstant("last_updated_at"));
            }
            if (!found) {
                System.out.println("NOT FOUND user_id=" + userId);
            }
        }
    }

    static long parseUserId(String[] args) {
        if (args.length != 2 || !"--user-id".equals(args[0])) {
            throw new IllegalArgumentException("usage: lookup-active-cart --user-id <positive-user-id>");
        }
        long userId = Long.parseLong(args[1]);
        if (userId <= 0) {
            throw new IllegalArgumentException("user ID must be positive");
        }
        return userId;
    }

    static String selectCql(String keyspace, String table) {
        return "SELECT user_id, item_id, category_id, added_at, last_updated_at FROM "
                + keyspace + "." + table + " WHERE user_id = ?";
    }
}
