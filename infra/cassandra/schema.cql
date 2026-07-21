-- Run after Astra keyspace provisioning with CASSANDRA_KEYSPACE selected.
-- The bootstrap is idempotent. Application code must not create keyspaces.

CREATE TABLE IF NOT EXISTS user_active_cart (
    user_id bigint,
    item_id bigint,
    category_id bigint,
    added_at timestamp,
    last_updated_at timestamp,
    PRIMARY KEY ((user_id), item_id)
);
