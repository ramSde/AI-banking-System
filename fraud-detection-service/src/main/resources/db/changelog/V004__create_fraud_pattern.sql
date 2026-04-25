-- Fraud Pattern Table
-- Stores detected fraud patterns

CREATE TABLE fraud_pattern (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pattern_type VARCHAR(50) NOT NULL,
    pattern_name VARCHAR(100) NOT NULL,
    description TEXT,
    pattern_data JSONB NOT NULL,
    occurrences INTEGER NOT NULL DEFAULT 1,
    first_detected_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_detected_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE fraud_pattern IS 'Detected fraud patterns';
COMMENT ON COLUMN fraud_pattern.id IS 'Primary key';
COMMENT ON COLUMN fraud_pattern.pattern_type IS 'Type: VELOCITY_SPIKE, GEOGRAPHIC_ANOMALY, AMOUNT_PATTERN';
COMMENT ON COLUMN fraud_pattern.pattern_name IS 'Pattern name';
COMMENT ON COLUMN fraud_pattern.description IS 'Pattern description';
COMMENT ON COLUMN fraud_pattern.pattern_data IS 'Pattern details as JSON';
COMMENT ON COLUMN fraud_pattern.occurrences IS 'Number of times pattern detected';
COMMENT ON COLUMN fraud_pattern.first_detected_at IS 'First detection timestamp';
COMMENT ON COLUMN fraud_pattern.last_detected_at IS 'Last detection timestamp';
COMMENT ON COLUMN fraud_pattern.created_at IS 'Creation timestamp';
COMMENT ON COLUMN fraud_pattern.updated_at IS 'Last update timestamp';
COMMENT ON COLUMN fraud_pattern.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN fraud_pattern.version IS 'Optimistic locking version';

-- Rollback
--rollback DROP TABLE fraud_pattern;
