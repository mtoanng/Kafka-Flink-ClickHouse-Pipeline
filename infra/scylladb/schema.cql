-- Taobao Phase 4 current-state serving table.
-- For a managed multi-node deployment, replace SimpleStrategy with the
-- provider's NetworkTopologyStrategy and its documented datacenter name.

CREATE KEYSPACE IF NOT EXISTS taobao_behavior
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

CREATE TABLE IF NOT EXISTS taobao_behavior.user_current_activity (
    user_id bigint PRIMARY KEY,
    last_event_time timestamp,
    last_behavior_type text,
    last_item_id bigint,
    last_category_id bigint,
    session_event_count bigint,
    updated_at timestamp
);
