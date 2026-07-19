package com.taobao.behavior.sink;

import com.taobao.behavior.avro.UserBehaviorEvent;
import com.taobao.behavior.model.ItemMetrics1m;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

final class ClickHouseRowMapper {
    private ClickHouseRowMapper() {}

    static Map<String, Object> rawValues(UserBehaviorEvent event) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("event_id", event.getEventId().toString());
        row.put("user_id", event.getUserId());
        row.put("item_id", event.getItemId());
        row.put("category_id", event.getCategoryId());
        row.put("behavior_type", event.getBehaviorType().toString());
        row.put("event_time", utcTime(event.getEventTimeMs()));
        row.put("replay_run_id", event.getReplayRunId().toString());
        return row;
    }

    static Map<String, Object> itemMetricsValues(ItemMetrics1m metrics) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("window_start", utcTime(metrics.getWindowStart()));
        row.put("item_id", metrics.getItemId());
        row.put("category_id", metrics.getCategoryId());
        row.put("pv_count", metrics.getPvCount());
        row.put("cart_count", metrics.getCartCount());
        row.put("fav_count", metrics.getFavCount());
        row.put("buy_count", metrics.getBuyCount());
        row.put("unique_users", metrics.getUniqueUsers());
        row.put("replay_run_id", metrics.getReplayRunId());
        return row;
    }

    private static ZonedDateTime utcTime(long epochMillis) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC);
    }
}
