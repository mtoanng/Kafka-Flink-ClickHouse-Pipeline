package com.taobao.behavior.sink;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ScyllaCqlContractTest {
    @Test
    void ddlAndPreparedInsertUseUserIdAsTheOnlyPartitionKey() throws IOException {
        String ddl = Files.readString(repositoryRoot().resolve("infra/scylladb/schema.cql"));

        assertTrue(ddl.contains("CREATE TABLE IF NOT EXISTS taobao_behavior.user_current_activity"));
        assertTrue(ddl.contains("user_id bigint PRIMARY KEY"));
        assertTrue(ddl.contains("last_event_time timestamp"));
        assertTrue(ddl.contains("last_behavior_type text"));
        assertTrue(ddl.contains("last_item_id bigint"));
        assertTrue(ddl.contains("last_category_id bigint"));
        assertTrue(ddl.contains("session_event_count bigint"));
        assertTrue(ddl.contains("updated_at timestamp"));
        assertEquals(
                "INSERT INTO taobao_behavior.user_current_activity "
                        + "(user_id, last_event_time, last_behavior_type, last_item_id, "
                        + "last_category_id, session_event_count, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                ScyllaCurrentActivitySink.insertCql("taobao_behavior", "user_current_activity"));
    }

    @Test
    void rejectsIdentifiersInsteadOfInterpolatingUntrustedCql() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ScyllaCurrentActivitySink.insertCql("taobao; DROP KEYSPACE x", "activity"));
    }

    private static Path repositoryRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("infra/scylladb/schema.cql"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("could not locate repository root");
    }
}
