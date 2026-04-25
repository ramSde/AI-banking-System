-- Fraud Rule Table
-- Stores configurable fraud detection rules

CREATE TABLE fraud_rule (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_name VARCHAR(100) NOT NULL,
    rule_type VARCHAR(50) NOT NULL,
    description TEXT,
    rule_config JSONB NOT NULL,
    weight INTEGER NOT NULL DEFAULT 10,
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_fraud_rule_name UNIQUE (rule_name)
);

COMMENT ON TABLE fraud_rule IS 'Configurable fraud detection rules';
COMMENT ON COLUMN fraud_rule.id IS 'Primary key';
COMMENT ON COLUMN fraud_rule.rule_name IS 'Unique rule name';
COMMENT ON COLUMN fraud_rule.rule_type IS 'Type: VELOCITY, AMOUNT, GEOGRAPHIC, TIME_PATTERN, ACCOUNT_AGE';
COMMENT ON COLUMN fraud_rule.description IS 'Rule description';
COMMENT ON COLUMN fraud_rule.rule_config IS 'Rule configuration as JSON';
COMMENT ON COLUMN fraud_rule.weight IS 'Rule weight for score calculation (1-100)';
COMMENT ON COLUMN fraud_rule.enabled IS 'Whether rule is active';
COMMENT ON COLUMN fraud_rule.created_by IS 'User who created the rule';
COMMENT ON COLUMN fraud_rule.created_at IS 'Creation timestamp';
COMMENT ON COLUMN fraud_rule.updated_at IS 'Last update timestamp';
COMMENT ON COLUMN fraud_rule.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN fraud_rule.version IS 'Optimistic locking version';

-- Rollback
--rollback DROP TABLE fraud_rule;
