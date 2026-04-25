-- Fraud Alert Table
-- Stores fraud alerts for investigation

CREATE TABLE fraud_alert (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fraud_check_id UUID NOT NULL,
    transaction_id UUID NOT NULL,
    user_id UUID NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    description TEXT,
    alert_details JSONB,
    assigned_to UUID,
    resolved_at TIMESTAMPTZ,
    resolution_notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE fraud_alert IS 'Fraud alerts requiring investigation';
COMMENT ON COLUMN fraud_alert.id IS 'Primary key';
COMMENT ON COLUMN fraud_alert.fraud_check_id IS 'Associated fraud check';
COMMENT ON COLUMN fraud_alert.transaction_id IS 'Transaction that triggered alert';
COMMENT ON COLUMN fraud_alert.user_id IS 'User associated with alert';
COMMENT ON COLUMN fraud_alert.alert_type IS 'Type: HIGH_RISK, SUSPICIOUS_PATTERN, BLOCKED_TRANSACTION';
COMMENT ON COLUMN fraud_alert.severity IS 'Severity: LOW, MEDIUM, HIGH, CRITICAL';
COMMENT ON COLUMN fraud_alert.status IS 'Status: OPEN, INVESTIGATING, RESOLVED, FALSE_POSITIVE';
COMMENT ON COLUMN fraud_alert.description IS 'Alert description';
COMMENT ON COLUMN fraud_alert.alert_details IS 'Detailed alert information as JSON';
COMMENT ON COLUMN fraud_alert.assigned_to IS 'Admin user assigned to investigate';
COMMENT ON COLUMN fraud_alert.resolved_at IS 'When alert was resolved';
COMMENT ON COLUMN fraud_alert.resolution_notes IS 'Resolution notes';
COMMENT ON COLUMN fraud_alert.created_at IS 'Creation timestamp';
COMMENT ON COLUMN fraud_alert.updated_at IS 'Last update timestamp';
COMMENT ON COLUMN fraud_alert.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN fraud_alert.version IS 'Optimistic locking version';

-- Rollback
--rollback DROP TABLE fraud_alert;
