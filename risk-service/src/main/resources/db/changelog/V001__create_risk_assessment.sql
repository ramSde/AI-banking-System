-- liquibase formatted sql

-- changeset risk-service:1
-- comment: Create risk_assessment table for storing risk assessment results

CREATE TABLE risk_assessment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    session_id UUID NOT NULL,
    risk_score INTEGER NOT NULL CHECK (risk_score >= 0 AND risk_score <= 100),
    risk_level VARCHAR(20) NOT NULL CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH')),
    risk_action VARCHAR(20) NOT NULL CHECK (risk_action IN ('ALLOW', 'REQUIRE_MFA', 'BLOCK')),
    device_fingerprint VARCHAR(255),
    ip_address VARCHAR(45) NOT NULL,
    geolocation JSONB,
    factors JSONB NOT NULL,
    assessed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

-- Indexes for query performance
CREATE INDEX idx_risk_assessment_user_id ON risk_assessment(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_risk_assessment_session_id ON risk_assessment(session_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_risk_assessment_risk_level ON risk_assessment(risk_level) WHERE deleted_at IS NULL;
CREATE INDEX idx_risk_assessment_assessed_at ON risk_assessment(assessed_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_risk_assessment_device_fingerprint ON risk_assessment(device_fingerprint) WHERE deleted_at IS NULL;
CREATE INDEX idx_risk_assessment_ip_address ON risk_assessment(ip_address) WHERE deleted_at IS NULL;

-- Composite index for common queries
CREATE INDEX idx_risk_assessment_user_assessed ON risk_assessment(user_id, assessed_at DESC) WHERE deleted_at IS NULL;

-- Comments
COMMENT ON TABLE risk_assessment IS 'Stores risk assessment results for authentication attempts';
COMMENT ON COLUMN risk_assessment.id IS 'Unique identifier for the risk assessment';
COMMENT ON COLUMN risk_assessment.user_id IS 'User being assessed';
COMMENT ON COLUMN risk_assessment.session_id IS 'Session identifier for the authentication attempt';
COMMENT ON COLUMN risk_assessment.risk_score IS 'Calculated risk score (0-100)';
COMMENT ON COLUMN risk_assessment.risk_level IS 'Risk level: LOW, MEDIUM, HIGH';
COMMENT ON COLUMN risk_assessment.risk_action IS 'Action to take: ALLOW, REQUIRE_MFA, BLOCK';
COMMENT ON COLUMN risk_assessment.device_fingerprint IS 'Device fingerprint hash';
COMMENT ON COLUMN risk_assessment.ip_address IS 'IP address of the authentication attempt';
COMMENT ON COLUMN risk_assessment.geolocation IS 'Geographic location data (country, city, lat, lon)';
COMMENT ON COLUMN risk_assessment.factors IS 'Breakdown of risk factors and their scores';
COMMENT ON COLUMN risk_assessment.assessed_at IS 'Timestamp when assessment was performed';

-- rollback DROP TABLE risk_assessment;
