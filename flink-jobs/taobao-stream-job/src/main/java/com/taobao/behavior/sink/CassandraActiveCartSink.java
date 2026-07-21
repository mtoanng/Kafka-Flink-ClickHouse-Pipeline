package com.taobao.behavior.sink;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.taobao.behavior.model.ActiveCartItem;
import com.taobao.behavior.model.CartMutation;
import java.time.Instant;
import java.util.regex.Pattern;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

public class CassandraActiveCartSink extends RichSinkFunction<CartMutation> {
    private static final Pattern CQL_IDENTIFIER = Pattern.compile("[a-z][a-z0-9_]*");

    private final CassandraConfig config;
    private final CassandraSessionFactory sessionFactory;
    private transient CqlSession session;
    private transient PreparedStatement upsertStatement;
    private transient PreparedStatement deleteStatement;

    public CassandraActiveCartSink(CassandraConfig config) {
        this(config, new CassandraSessionFactory());
    }

    CassandraActiveCartSink(CassandraConfig config, CassandraSessionFactory sessionFactory) {
        this.config = config;
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void open(Configuration parameters) {
        session = sessionFactory.open(config);
        upsertStatement = session.prepare(upsertCql(config.keyspace(), config.table()));
        deleteStatement = session.prepare(deleteCql(config.keyspace(), config.table()));
    }

    @Override
    public void invoke(CartMutation mutation, Context context) {
        validateMutation(mutation);
        ActiveCartItem item = mutation.getItem();
        if (mutation.getType() == CartMutation.Type.UPSERT_CART_ITEM) {
            session.execute(upsertStatement.bind(
                    item.getUserId(),
                    item.getItemId(),
                    item.getCategoryId(),
                    Instant.ofEpochMilli(item.getAddedAtMs()),
                    Instant.ofEpochMilli(item.getLastUpdatedAtMs())));
            return;
        }
        session.execute(deleteStatement.bind(item.getUserId(), item.getItemId()));
    }

    @Override
    public void close() {
        if (session != null) {
            session.close();
            session = null;
            upsertStatement = null;
            deleteStatement = null;
        }
    }

    static String upsertCql(String keyspace, String table) {
        return "INSERT INTO " + qualifiedTable(keyspace, table)
                + " (user_id, item_id, category_id, added_at, last_updated_at) VALUES (?, ?, ?, ?, ?)";
    }

    static String deleteCql(String keyspace, String table) {
        return "DELETE FROM " + qualifiedTable(keyspace, table) + " WHERE user_id = ? AND item_id = ?";
    }

    private static String qualifiedTable(String keyspace, String table) {
        return validIdentifier(keyspace, "keyspace") + "." + validIdentifier(table, "table");
    }

    private static void validateMutation(CartMutation mutation) {
        if (mutation == null || mutation.getType() == null || mutation.getItem() == null) {
            throw new IllegalArgumentException("cart mutation and item are required");
        }
        ActiveCartItem item = mutation.getItem();
        if (item.getUserId() <= 0 || item.getItemId() <= 0 || item.getCategoryId() <= 0
                || item.getAddedAtMs() < 0 || item.getLastUpdatedAtMs() < 0) {
            throw new IllegalArgumentException("cart mutation must contain valid user and item values");
        }
    }

    private static String validIdentifier(String value, String name) {
        if (!CQL_IDENTIFIER.matcher(value).matches()) {
            throw new IllegalArgumentException(name + " must be a lowercase CQL identifier");
        }
        return value;
    }
}
