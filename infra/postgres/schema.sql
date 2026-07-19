CREATE TABLE IF NOT EXISTS behavior_rules (
    rule_id TEXT PRIMARY KEY,
    rule_type TEXT NOT NULL,
    threshold_seconds INTEGER NOT NULL,
    enabled BOOLEAN NOT NULL,
    version BIGINT NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT behavior_rules_rule_type_ck
        CHECK (rule_type IN ('cart_abandonment')),
    CONSTRAINT behavior_rules_rule_id_ck
        CHECK (rule_id = 'cart_abandonment'),
    CONSTRAINT behavior_rules_threshold_ck
        CHECK (threshold_seconds > 0),
    CONSTRAINT behavior_rules_version_ck
        CHECK (version >= 1)
);

COMMENT ON TABLE behavior_rules IS
    'Control-plane rules only; analytical history remains in ClickHouse.';
