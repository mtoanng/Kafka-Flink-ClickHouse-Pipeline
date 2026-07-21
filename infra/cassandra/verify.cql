-- Replace 100 with the user ID to inspect active cart items.
SELECT user_id, item_id, category_id, added_at, last_updated_at
FROM user_active_cart
WHERE user_id = 100;
