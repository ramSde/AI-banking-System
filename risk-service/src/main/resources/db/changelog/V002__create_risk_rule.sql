-- liquibase formatted sql

-- changeset risk-service:2
-- comment: Create risk_rule table for configurable risk rules

CREATE TABLE risk_rule (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    rule_type VARCHAR(50) NOT NULL CHECK (rule_type IN ('DEVICE', 'LOCATION', 'VELOCITY', 'TIME', 'FAILED_ATTEMPTS', 'CUSTOM')),
    condition JSONB NOT NULL,
    risk_score_impact INTEGER NOT NULL CHECK (risk_score_impact >= 0 AND risk_score_impact <= 100),
    enabled BOOLEAN NOT NULL DEFAULT true,
    priority INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

-- Indexes
CREATE INDEX idx_risk_rule_rule_type ON risk_rule(rule_type) WHERE deleted_at IS NULL AND enabled = true;
CREATE INDEX idx_risk_rule_enabled ON risk_rule(enabled) WHERE deleted_at IS NULL;
CREATE INDEX idx_risk_rule_priority ON risk_rule(priority DESC) WHERE deleted_at IS NULL AND enabled = true;

-- Comments
COMMENT ON TABLE risk_rule IS 'Configurable risk rules for risk assessment';
COMMENT ON COLUMN risk_rule.id IS 'Unique identifier for the risk rule';
COMMENT ON COLUMN risk_rule.name IS 'Unique name for the risk rule';
COMMENT ON COLUMN risk_rule.description IS 'Human-readable description of the rule';
COMMENT ON COLUMN risk_rule.rule_type IS 'Type of risk rule: DEVICE, LOCATION, VELOCITY, TIME, FAILED_ATTEMPTS, CUSTOM';
COMMENT ON COLUMN risk_rule.condition IS 'JSON condition for rule evaluation';
COMMENT ON COLUMN risk_rule.risk_score_impact IS 'Impact on risk score when rule matches (0-100)';
COMMENT ON COLUMN risk_rule.enabled IS 'Whether the rule is active';
COMMENT ON COLUMN risk_rule.priority IS 'Rule evaluation priority (higher = evaluated first)';

-- rollback DROP TABLE risk_rule;
