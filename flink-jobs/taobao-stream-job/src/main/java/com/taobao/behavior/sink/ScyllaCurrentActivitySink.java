package com.taobao.behavior.sink;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.taobao.behavior.model.UserCurrentActivity;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.regex.Pattern;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

public class ScyllaCurrentActivitySink extends RichSinkFunction<UserCurrentActivity> {
    private static final Pattern CQL_IDENTIFIER = Pattern.compile("[a-z][a-z0-9_]*");

    private final String host;
    private final int port;
    private final String localDatacenter;
    private final String user;
    private final String password;
    private final String keyspace;
    private final String table;

    private transient CqlSession session;
    private transient PreparedStatement insertStatement;

    public ScyllaCurrentActivitySink(
            String host,
            int port,
            String localDatacenter,
            String user,
            String password,
            String keyspace,
            String table) {
        this.host = host;
        this.port = port;
        this.localDatacenter = localDatacenter;
        this.user = user;
        this.password = password;
        this.keyspace = validIdentifier(keyspace, "keyspace");
        this.table = validIdentifier(table, "table");
    }

    @Override
    public void open(Configuration parameters) {
        CqlSessionBuilder builder =
                CqlSession.builder()
                        .addContactPoint(new InetSocketAddress(host, port))
                        .withLocalDatacenter(localDatacenter);
        if (!user.isBlank()) {
            builder.withAuthCredentials(user, password);
        }
        session = builder.build();
        insertStatement = session.prepare(insertCql(keyspace, table));
    }

    @Override
    public void invoke(UserCurrentActivity activity, Context context) {
        session.execute(
                insertStatement.bind(
                        activity.getUserId(),
                        Instant.ofEpochMilli(activity.getLastEventTimeMs()),
                        activity.getLastBehaviorType(),
                        activity.getLastItemId(),
                        activity.getLastCategoryId(),
                        activity.getSessionEventCount(),
                        Instant.ofEpochMilli(activity.getUpdatedAtMs())));
    }

    @Override
    public void close() {
        if (session != null) {
            session.close();
        }
    }

    static String insertCql(String keyspace, String table) {
        return "INSERT INTO "
                + validIdentifier(keyspace, "keyspace")
                + "."
                + validIdentifier(table, "table")
                + " (user_id, last_event_time, last_behavior_type, last_item_id, "
                + "last_category_id, session_event_count, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    private static String validIdentifier(String value, String name) {
        if (!CQL_IDENTIFIER.matcher(value).matches()) {
            throw new IllegalArgumentException(name + " must be a lowercase CQL identifier");
        }
        return value;
    }
}
