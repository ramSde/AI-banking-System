-- liquibase formatted sql

-- changeset ai-insight-service:3
-- comment: Create recommendations table for storing personalized financial recommendations

CREATE TABLE recommendations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    insight_id UUID,
    recommendation_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    action_items JSONB NOT NULL,
    potential_savings DECIMAL(15,2),
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    category VARCHAR(100),
    confidence_score DECIMAL(5,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    is_accepted BOOLEAN NOT NULL DEFAULT FALSE,
    is_dismissed BOOLEAN NOT NULL DEFAULT FALSE,
    accepted_at TIMESTAMP WITH TIME ZONE,
    dismissed_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_recommendations_insight FOREIGN KEY (insight_id) REFERENCES insights(id) ON DELETE SET NULL
);

-- Create indexes
CREATE INDEX idx_recommendations_user_id ON recommendations(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_recommendations_insight_id ON recommendations(insight_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_recommendations_type ON recommendations(recommendation_type) WHERE deleted_at IS NULL;
CREATE INDEX idx_recommendations_priority ON recommendations(priority) WHERE deleted_at IS NULL;
CREATE INDEX idx_recommendations_status ON recommendations(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_recommendations_category ON recommendations(category) WHERE deleted_at IS NULL;
CREATE INDEX idx_recommendations_expires_at ON recommendations(expires_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_recommendations_user_pending ON recommendations(user_id, status) WHERE deleted_at IS NULL AND status = 'PENDING';
CREATE INDEX idx_recommendations_action_items ON recommendations USING gin(action_items) WHERE deleted_at IS NULL;

-- Add comments
COMMENT ON TABLE recommendations IS 'Stores personalized financial recommendations';
COMMENT ON COLUMN recommendations.recommendation_type IS 'Type: SAVE_MONEY, REDUCE_SPENDING, OPTIMIZE_BUDGET, INVESTMENT, DEBT_REDUCTION, SUBSCRIPTION_REVIEW';
COMMENT ON COLUMN recommendations.priority IS 'Priority: LOW, MEDIUM, HIGH, CRITICAL';
COMMENT ON COLUMN recommendations.status IS 'Status: PENDING, ACCEPTED, DISMISSED, EXPIRED, COMPLETED';
COMMENT ON COLUMN recommendations.action_items IS 'List of actionable steps in JSON format';

-- rollback DROP TABLE recommendations;
