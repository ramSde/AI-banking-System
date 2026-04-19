-- Liquibase formatted SQL
-- changeset device-service:3

-- Create device_anomaly table
CREATE TABLE device_anomaly (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id UUID NOT NULL,
    user_id UUID NOT NULL,
    anomaly_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    
    -- Anomaly Details
    description TEXT NOT NULL,
    detected_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    resolved_at TIMESTAMPTZ,
    resolution_notes TEXT,
    
    -- Context Information
    ip_address VARCHAR(45),
    geolocation JSONB,
    user_agent TEXT,
    
    -- Risk Assessment
    risk_score INTEGER NOT NULL DEFAULT 50,
    confidence_level DECIMAL(3,2) NOT NULL DEFAULT 0.75,
    
    -- Metadata
    metadata JSONB,
    
    -- Audit Fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT fk_device_anomaly_device FOREIGN KEY (device_id) REFERENCES device(id) ON DELETE CASCADE,
    CONSTRAINT chk_anomaly_type CHECK (anomaly_type IN (
        'NEW_DEVICE', 'LOCATION_CHANGE', 'IMPOSSIBLE_TRAVEL', 
        'UNUSUAL_HOURS', 'MULTIPLE_LOCATIONS', 'DEVICE_CHANGE',
        'SUSPICIOUS_PATTERN', 'VELOCITY_ANOMALY', 'GEOFENCE_VIOLATION'
    )),
    CONSTRAINT chk_severity CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT chk_risk_score CHECK (risk_score >= 0 AND risk_score <= 100),
    CONSTRAINT chk_confidence_level CHECK (confidence_level >= 0.0 AND confidence_level <= 1.0)
);

-- Indexes
CREATE INDEX idx_device_anomaly_device_id ON device_anomaly(device_id);
CREATE INDEX idx_device_anomaly_user_id ON device_anomaly(user_id);
CREATE INDEX idx_device_anomaly_type ON device_anomaly(anomaly_type);
CREATE INDEX idx_device_anomaly_severity ON device_anomaly(severity);
CREATE INDEX idx_device_anomaly_detected_at ON device_anomaly(detected_at);
CREATE INDEX idx_device_anomaly_risk_score ON device_anomaly(risk_score);
CREATE INDEX idx_device_anomaly_resolved ON device_anomaly(resolved_at) WHERE resolved_at IS NULL;
CREATE INDEX idx_device_anomaly_deleted_at ON device_anomaly(deleted_at) WHERE deleted_at IS NULL;

-- Composite Indexes
CREATE INDEX idx_device_anomaly_device_detected ON device_anomaly(device_id, detected_at DESC);
CREATE INDEX idx_device_anomaly_user_detected ON device_anomaly(user_id, detected_at DESC);
CREATE INDEX idx_device_anomaly_severity_detected ON device_anomaly(severity, detected_at DESC);

-- Comments
COMMENT ON TABLE device_anomaly IS 'Stores detected device anomalies and suspicious activities';
COMMENT ON COLUMN device_anomaly.id IS 'Unique anomaly identifier';
COMMENT ON COLUMN device_anomaly.device_id IS 'Device where anomaly was detected';
COMMENT ON COLUMN device_anomaly.user_id IS 'User associated with the anomaly';
COMMENT ON COLUMN device_anomaly.anomaly_type IS 'Type of anomaly detected';
COMMENT ON COLUMN device_anomaly.severity IS 'Severity level: LOW, MEDIUM, HIGH, CRITICAL';
COMMENT ON COLUMN device_anomaly.risk_score IS 'Risk score from 0 (safe) to 100 (dangerous)';
COMMENT ON COLUMN device_anomaly.confidence_level IS 'Confidence in anomaly detection (0.0-1.0)';
COMMENT ON COLUMN device_anomaly.geolocation IS 'JSON object with location data when anomaly occurred';
COMMENT ON COLUMN device_anomaly.metadata IS 'Additional anomaly-specific data';
COMMENT ON COLUMN device_anomaly.version IS 'Optimistic locking version';

-- rollback DROP TABLE device_anomaly;