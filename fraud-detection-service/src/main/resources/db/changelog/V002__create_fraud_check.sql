-- Fraud Check Table
-- Stores fraud check results for each transaction

CREATE TABLE fraud_check (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL,
    user_id UUID NOT NULL,
    risk_score INTEGER NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    blocked BOOLEAN NOT NULL DEFAULT false,
    rules_triggered JSONB,
    check_details JSONB,
    checked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_risk_score CHECK (risk_score >= 0 AND risk_score <= 100)
);

COMMENT ON TABLE fraud_check IS 'Fraud check results for transactions';
COMMENT ON COLUMN fraud_check.id IS 'Primary key';
COMMENT ON COLUMN fraud_check.transaction_id IS 'Transaction being checked';
COMMENT ON COLUMN fraud_check.user_id IS 'User who initiated transaction';
COMMENT ON COLUMN fraud_check.risk_score IS 'Calculated risk score (0-100)';
COMMENT ON COLUMN fraud_check.risk_level IS 'Risk level: LOW, MEDIUM, HIGH';
COMMENT ON COLUMN fraud_check.blocked IS 'Whether transaction was blocked';
COMMENT ON COLUMN fraud_check.rules_triggered IS 'Rules that triggered as JSON array';
COMMENT ON COLUMN fraud_check.check_details IS 'Detailed check results as JSON';
COMMENT ON COLUMN fraud_check.checked_at IS 'When check was performed';
COMMENT ON COLUMN fraud_check.created_at IS 'Creation timestamp';
COMMENT ON COLUMN fraud_check.updated_at IS 'Last update timestamp';
COMMENT ON COLUMN fraud_check.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN fraud_check.version IS 'Optimistic locking version';

-- Rollback
--rollback DROP TABLE fraud_check;
