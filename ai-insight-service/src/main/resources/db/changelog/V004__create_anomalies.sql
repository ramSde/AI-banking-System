-- liquibase formatted sql

-- changeset ai-insight-service:4
-- comment: Create anomalies table for tracking detected spending anomalies

CREATE TABLE anomalies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    insight_id UUID,
    transaction_id UUID,
    anomaly_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    description TEXT NOT NULL,
    detected_value DECIMAL(15,2) NOT NULL,
    expected_value DECIMAL(15,2),
    deviation_percentage DECIMAL(5,2),
    z_score DECIMAL(10,4),
    category VARCHAR(100),
    merchant_name VARCHAR(255),
    detection_method VARCHAR(50) NOT NULL,
    confidence_score DECIMAL(5,2) NOT NULL,
    is_false_positive BOOLEAN NOT NULL DEFAULT FALSE,
    is_acknowledged BOOLEAN NOT NULL DEFAULT FALSE,
    acknowledged_at TIMESTAMP WITH TIME ZONE,
    resolution_notes TEXT,
    metadata JSONB,
    detected_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_anomalies_insight FOREIGN KEY (insight_id) REFERENCES insights(id) ON DELETE SET NULL
);

-- Create indexes
CREATE INDEX idx_anomalies_user_id ON anomalies(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_anomalies_insight_id ON anomalies(insight_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_anomalies_transaction_id ON anomalies(transaction_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_anomalies_type ON anomalies(anomaly_type) WHERE deleted_at IS NULL;
CREATE INDEX idx_anomalies_severity ON anomalies(severity) WHERE deleted_at IS NULL;
CREATE INDEX idx_anomalies_category ON anomalies(category) WHERE deleted_at IS NULL;
CREATE INDEX idx_anomalies_detected_at ON anomalies(detected_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_anomalies_user_unacknowledged ON anomalies(user_id, is_acknowledged) WHERE deleted_at IS NULL AND is_acknowledged = FALSE;
CREATE INDEX idx_anomalies_false_positive ON anomalies(is_false_positive) WHERE deleted_at IS NULL;

-- Add comments
COMMENT ON TABLE anomalies IS 'Tracks detected spending anomalies and unusual patterns';
COMMENT ON COLUMN anomalies.anomaly_type IS 'Type: UNUSUAL_AMOUNT, UNUSUAL_MERCHANT, UNUSUAL_CATEGORY, UNUSUAL_FREQUENCY, UNUSUAL_TIME';
COMMENT ON COLUMN anomalies.severity IS 'Severity: LOW, MEDIUM, HIGH, CRITICAL';
COMMENT ON COLUMN anomalies.detection_method IS 'Method: Z_SCORE, IQR, ISOLATION_FOREST, STATISTICAL';
COMMENT ON COLUMN anomalies.z_score IS 'Statistical Z-score for the anomaly';

-- rollback DROP TABLE anomalies;
