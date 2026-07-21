package com.taobao.behavior.sink;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CassandraCqlContractTest {
    @Test
    void ddlAndPreparedStatementsUseOnlyTheUserPartition() throws IOException {
        String ddl = Files.readString(repositoryRoot().resolve("infra/cassandra/schema.cql"));

        assertTrue(ddl.contains("CREATE TABLE IF NOT EXISTS user_active_cart"));
        assertTrue(ddl.contains("PRIMARY KEY ((user_id), item_id)"));
        assertTrue(!ddl.contains("CREATE KEYSPACE"));
        assertTrue(!ddl.contains("INDEX"));
        assertTrue(!ddl.contains("MATERIALIZED VIEW"));
        assertEquals(
                "INSERT INTO taobao_streaming.user_active_cart (user_id, item_id, category_id, added_at, last_updated_at) VALUES (?, ?, ?, ?, ?)",
                CassandraActiveCartSink.upsertCql("taobao_streaming", "user_active_cart"));
        assertEquals(
                "DELETE FROM taobao_streaming.user_active_cart WHERE user_id = ? AND item_id = ?",
                CassandraActiveCartSink.deleteCql("taobao_streaming", "user_active_cart"));
    }

    @Test
    void rejectsIdentifiersInsteadOfInterpolatingUntrustedCql() {
        assertThrows(IllegalArgumentException.class,
                () -> CassandraActiveCartSink.upsertCql("taobao; DROP KEYSPACE x", "activity"));
    }

    private static Path repositoryRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("infra/cassandra/schema.cql"))) return current;
            current = current.getParent();
        }
        throw new IllegalStateException("could not locate repository root");
    }
}
