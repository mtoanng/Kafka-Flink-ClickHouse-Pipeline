-- Deduplicated ClickHouse verification query pack. Replace fixture-run with the replay run ID.

SELECT count() AS accepted_raw_count
FROM taobao_behavior.raw_behavior_events_deduplicated
WHERE replay_run_id = 'fixture-run';

SELECT count() AS invalid_event_count
FROM taobao_behavior.invalid_behavior_events_deduplicated
WHERE replay_run_id = 'fixture-run';

SELECT count() AS late_event_count
FROM taobao_behavior.late_behavior_events_deduplicated
WHERE replay_run_id = 'fixture-run';

SELECT event_id, user_id, behavior_type, event_time
FROM taobao_behavior.raw_behavior_events_deduplicated
WHERE replay_run_id = 'fixture-run'
  AND item_id = 500
  AND event_time >= toDateTime64('2017-11-26 01:00:00', 3, 'UTC')
  AND event_time < toDateTime64('2017-11-26 01:01:00', 3, 'UTC')
ORDER BY event_time, user_id;

SELECT pv_count, cart_count, fav_count, buy_count, unique_users
FROM taobao_behavior.item_metrics_1m_deduplicated
WHERE replay_run_id = 'fixture-run'
  AND item_id = 500
  AND window_start = toDateTime64('2017-11-26 01:00:00', 3, 'UTC');

-- Physical minus logical rows reveals duplicates retained until background merges.
SELECT
    (SELECT count() FROM taobao_behavior.raw_behavior_events WHERE replay_run_id = 'fixture-run')
      -
    (SELECT count() FROM taobao_behavior.raw_behavior_events_deduplicated WHERE replay_run_id = 'fixture-run')
      AS duplicate_transport_rows;
