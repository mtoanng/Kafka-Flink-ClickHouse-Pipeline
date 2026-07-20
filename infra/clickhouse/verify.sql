-- ClickHouse verification query pack. Replace fixture-run with the replay run ID.

-- One accepted, valid source event is stored once per replay event.
SELECT count() AS raw_count
FROM taobao_behavior.raw_behavior_events
WHERE replay_run_id = 'fixture-run';

-- Inspect raw events for one item and event-time interval. This is the primary
-- access pattern that justifies the raw table ordering key.
SELECT event_id, user_id, behavior_type, event_time
FROM taobao_behavior.raw_behavior_events
WHERE item_id = 500
  AND event_time >= toDateTime64('2017-11-26 01:00:00', 3, 'UTC')
  AND event_time < toDateTime64('2017-11-26 01:01:00', 3, 'UTC')
ORDER BY event_time, user_id;

-- Fixture assertion: item 500 has one cart and one buy in its first minute.
SELECT pv_count, cart_count, fav_count, buy_count, unique_users
FROM taobao_behavior.item_metrics_1m
WHERE replay_run_id = 'fixture-run'
  AND item_id = 500
  AND window_start = toDateTime64('2017-11-26 01:00:00', 3, 'UTC');
