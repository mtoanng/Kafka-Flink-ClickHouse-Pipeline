package com.taobao.behavior;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ClickHouseDdlContractTest {
    @Test
    void ddlMatchesTheRawAndItemMetricsSinkContract() throws IOException {
        String ddl = Files.readString(repositoryRoot().resolve("infra/clickhouse/schema.sql"));

        assertTrue(ddl.contains("CREATE TABLE IF NOT EXISTS taobao_behavior.raw_behavior_events"));
        assertTrue(ddl.contains("event_id String"));
        assertTrue(ddl.contains("replay_run_id String"));
        assertTrue(ddl.contains("ingested_at DateTime64(3, 'UTC') DEFAULT now64(3)"));
        assertTrue(ddl.contains("ORDER BY (toDate(event_time), item_id, event_time, user_id)"));
        assertTrue(ddl.contains("CREATE TABLE IF NOT EXISTS taobao_behavior.item_metrics_1m"));
        assertTrue(ddl.contains("unique_users UInt64"));
        assertTrue(ddl.contains("ORDER BY (toDate(window_start), item_id, window_start, replay_run_id)"));
        assertFalse(ddl.contains("f1_telemetry"));
    }

    private static Path repositoryRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("infra/clickhouse/schema.sql"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("could not locate repository root");
    }
}
