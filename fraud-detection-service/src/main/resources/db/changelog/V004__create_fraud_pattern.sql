--liquibase formatted sql

--changeset fraud-detection:4
--comment: Create fraud_pattern table for tracking detected fraud patterns

CREATE TABLE fraud_pattern (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pattern_type VARCHAR(50) NOT NULL,
    pattern_name VARCHAR(100) NOT NULL,
    description TEXT,
    user_id UUID,
    detection_count INTEGER NOT NULL DEFAULT 1,
    first_detected_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_detected_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    pattern_data JSONB,
    severity VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE fraud_pattern IS 'Detected fraud patterns for behavioral analysis';
COMMENT ON COLUMN fraud_pattern.id IS 'Primary key UUID';
COMMENT ON COLUMN fraud_pattern.pattern_type IS 'Type of pattern (VELOCITY_SPIKE, GEOGRAPHIC_ANOMALY, AMOUNT_PATTERN, etc.)';
COMMENT ON COLUMN fraud_pattern.pattern_name IS 'Human-readable pattern name';
COMMENT ON COLUMN fraud_pattern.description IS 'Pattern description';
COMMENT ON COLUMN fraud_pattern.user_id IS 'User associated with pattern (if user-specific)';
COMMENT ON COLUMN fraud_pattern.detection_count IS 'Number of times pattern has been detected';
COMMENT ON COLUMN fraud_pattern.first_detected_at IS 'First detection timestamp';
COMMENT ON COLUMN fraud_pattern.last_detected_at IS 'Most recent detection timestamp';
COMMENT ON COLUMN fraud_pattern.pattern_data IS 'JSON data describing the pattern';
COMMENT ON COLUMN fraud_pattern.severity IS 'Pattern severity (LOW, MEDIUM, HIGH, CRITICAL)';
COMMENT ON COLUMN fraud_pattern.active IS 'Whether pattern is still active';
COMMENT ON COLUMN fraud_pattern.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN fraud_pattern.updated_at IS 'Record update timestamp';
COMMENT ON COLUMN fraud_pattern.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN fraud_pattern.version IS 'Optimistic locking version';

--rollback DROP TABLE fraud_pattern;
