-- Replace 100 with the user ID to inspect.
SELECT user_id, last_event_time, last_behavior_type, last_item_id,
       last_category_id, session_event_count, updated_at
FROM taobao_behavior.user_current_activity
WHERE user_id = 100;
