package com.taobao.behavior.sink;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.taobao.behavior.model.ActiveCartItem;
import com.taobao.behavior.model.CartMutation;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.flink.configuration.Configuration;
import org.junit.jupiter.api.Test;

class CassandraActiveCartSinkTest {
    @Test
    void preparesBothStatementsBindsMutationsAndClosesOneSession() throws Exception {
        AtomicBoolean closed = new AtomicBoolean();
        List<Object[]> bindings = new ArrayList<>();
        PreparedStatement prepared = (PreparedStatement) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[] {PreparedStatement.class}, (proxy, method, args) -> {
                    if (method.getName().equals("bind")) {
                        bindings.add((Object[]) args[0]);
                        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {BoundStatement.class},
                                (boundProxy, boundMethod, boundArgs) -> null);
                    }
                    return null;
                });
        CqlSession session = (CqlSession) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[] {CqlSession.class}, (proxy, method, args) -> {
                    if (method.getName().equals("prepare")) return prepared;
                    if (method.getName().equals("close")) closed.set(true);
                    return null;
                });
        CassandraActiveCartSink sink = new CassandraActiveCartSink(config(), new CassandraSessionFactory() {
            @Override public CqlSession open(CassandraConfig ignored) { return session; }
        });
        ActiveCartItem item = new ActiveCartItem(100L, 500L, 50L, 1_000L, 2_000L);

        sink.open(new Configuration());
        sink.invoke(new CartMutation(CartMutation.Type.UPSERT_CART_ITEM, item), null);
        sink.invoke(new CartMutation(CartMutation.Type.DELETE_CART_ITEM, item), null);
        sink.close();

        assertEquals(2, bindings.size());
        assertEquals(5, bindings.get(0).length);
        assertEquals(100L, bindings.get(0)[0]);
        assertEquals(500L, bindings.get(1)[1]);
        assertTrue(closed.get());
    }

    @Test
    void rejectsMalformedMutationsBeforeWriting() throws Exception {
        CassandraActiveCartSink sink = new CassandraActiveCartSink(config(), new CassandraSessionFactory());
        assertThrows(IllegalArgumentException.class, () -> sink.invoke(null, null));
    }

    @Test
    void sessionInitializationFailureHasContextWithoutCredentials() throws Exception {
        CassandraActiveCartSink sink = new CassandraActiveCartSink(
                config(),
                new CassandraSessionFactory() {
                    @Override
                    public CqlSession open(CassandraConfig ignored) {
                        throw new IllegalStateException("connection refused");
                    }
                });

        IllegalStateException error = assertThrows(
                IllegalStateException.class, () -> sink.open(new Configuration()));

        assertTrue(error.getMessage().contains("Cassandra astra sink"));
        assertTrue(!error.getMessage().contains("test-token"));
    }

    private static CassandraConfig config() throws Exception {
        return CassandraConfig.fromEnvironment(Map.of(
                "CASSANDRA_MODE", "astra",
                "CASSANDRA_KEYSPACE", "taobao_streaming",
                "CASSANDRA_TABLE", "user_active_cart",
                "ASTRA_DB_SECURE_BUNDLE_PATH", Files.createTempFile("secure-connect-", ".zip").toString(),
                "ASTRA_DB_APPLICATION_TOKEN", "test-token"));
    }
}
