--liquibase formatted sql

--changeset fraud-detection:3
--comment: Create fraud_alert table for tracking fraud alerts

CREATE TABLE fraud_alert (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fraud_check_id UUID NOT NULL,
    transaction_id UUID NOT NULL,
    user_id UUID NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    description TEXT,
    assigned_to UUID,
    resolved_at TIMESTAMPTZ,
    resolution_notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE fraud_alert IS 'Fraud alerts requiring investigation or action';
COMMENT ON COLUMN fraud_alert.id IS 'Primary key UUID';
COMMENT ON COLUMN fraud_alert.fraud_check_id IS 'Reference to fraud check that triggered alert';
COMMENT ON COLUMN fraud_alert.transaction_id IS 'Transaction that triggered alert';
COMMENT ON COLUMN fraud_alert.user_id IS 'User associated with alert';
COMMENT ON COLUMN fraud_alert.alert_type IS 'Type of alert (HIGH_RISK, BLOCKED, PATTERN_DETECTED, etc.)';
COMMENT ON COLUMN fraud_alert.severity IS 'Alert severity (LOW, MEDIUM, HIGH, CRITICAL)';
COMMENT ON COLUMN fraud_alert.status IS 'Alert status (OPEN, INVESTIGATING, RESOLVED, FALSE_POSITIVE)';
COMMENT ON COLUMN fraud_alert.description IS 'Human-readable alert description';
COMMENT ON COLUMN fraud_alert.assigned_to IS 'Admin user assigned to investigate';
COMMENT ON COLUMN fraud_alert.resolved_at IS 'Timestamp when alert was resolved';
COMMENT ON COLUMN fraud_alert.resolution_notes IS 'Notes from investigation/resolution';
COMMENT ON COLUMN fraud_alert.created_at IS 'Alert creation timestamp';
COMMENT ON COLUMN fraud_alert.updated_at IS 'Alert update timestamp';
COMMENT ON COLUMN fraud_alert.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN fraud_alert.version IS 'Optimistic locking version';

--rollback DROP TABLE fraud_alert;
