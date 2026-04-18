-- liquibase formatted sql

-- changeset risk-service:3
-- comment: Create risk_history table for tracking risk assessment history

CREATE TABLE risk_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    assessment_id UUID NOT NULL,
    risk_score INTEGER NOT NULL CHECK (risk_score >= 0 AND risk_score <= 100),
    risk_level VARCHAR(20) NOT NULL CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH')),
    action_taken VARCHAR(20) NOT NULL CHECK (action_taken IN ('ALLOW', 'REQUIRE_MFA', 'BLOCK')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_risk_history_assessment FOREIGN KEY (assessment_id) REFERENCES risk_assessment(id)
);

-- Indexes
CREATE INDEX idx_risk_history_user_id ON risk_history(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_risk_history_assessment_id ON risk_history(assessment_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_risk_history_created_at ON risk_history(created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_risk_history_user_created ON risk_history(user_id, created_at DESC) WHERE deleted_at IS NULL;

-- Comments
COMMENT ON TABLE risk_history IS 'Historical record of risk assessments for analytics and auditing';
COMMENT ON COLUMN risk_history.id IS 'Unique identifier for the history record';
COMMENT ON COLUMN risk_history.user_id IS 'User associated with the assessment';
COMMENT ON COLUMN risk_history.assessment_id IS 'Reference to the risk assessment';
COMMENT ON COLUMN risk_history.risk_score IS 'Risk score at the time of assessment';
COMMENT ON COLUMN risk_history.risk_level IS 'Risk level at the time of assessment';
COMMENT ON COLUMN risk_history.action_taken IS 'Action that was taken based on the assessment';

-- rollback DROP TABLE risk_history;
