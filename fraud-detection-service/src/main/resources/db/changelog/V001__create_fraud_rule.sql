--liquibase formatted sql

--changeset fraud-detection:1
--comment: Create fraud_rule table for configurable fraud detection rules

CREATE TABLE fraud_rule (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_name VARCHAR(100) NOT NULL UNIQUE,
    rule_type VARCHAR(50) NOT NULL,
    description TEXT,
    rule_config JSONB NOT NULL,
    weight INTEGER NOT NULL DEFAULT 10,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE fraud_rule IS 'Configurable fraud detection rules with JSON-based configuration';
COMMENT ON COLUMN fraud_rule.id IS 'Primary key UUID';
COMMENT ON COLUMN fraud_rule.rule_name IS 'Unique rule name identifier';
COMMENT ON COLUMN fraud_rule.rule_type IS 'Type of fraud rule (VELOCITY, AMOUNT, GEOGRAPHIC, TIME_PATTERN, etc.)';
COMMENT ON COLUMN fraud_rule.description IS 'Human-readable description of the rule';
COMMENT ON COLUMN fraud_rule.rule_config IS 'JSON configuration for rule parameters';
COMMENT ON COLUMN fraud_rule.weight IS 'Weight/contribution to overall risk score (1-100)';
COMMENT ON COLUMN fraud_rule.enabled IS 'Whether the rule is active';
COMMENT ON COLUMN fraud_rule.created_by IS 'User ID who created the rule';
COMMENT ON COLUMN fraud_rule.created_at IS 'Timestamp when rule was created';
COMMENT ON COLUMN fraud_rule.updated_at IS 'Timestamp when rule was last updated';
COMMENT ON COLUMN fraud_rule.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN fraud_rule.version IS 'Optimistic locking version';

--rollback DROP TABLE fraud_rule;
