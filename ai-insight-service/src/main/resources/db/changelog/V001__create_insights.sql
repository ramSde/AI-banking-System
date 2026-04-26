-- liquibase formatted sql

-- changeset ai-insight-service:1
-- comment: Create insights table for storing AI-generated financial insights

CREATE TABLE insights (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    insight_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(100),
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    confidence_score DECIMAL(5,2) NOT NULL,
    metadata JSONB,
    ai_model VARCHAR(100),
    ai_prompt_tokens INTEGER,
    ai_completion_tokens INTEGER,
    ai_cost DECIMAL(10,6),
    valid_from TIMESTAMP WITH TIME ZONE NOT NULL,
    valid_until TIMESTAMP WITH TIME ZONE,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    is_dismissed BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP WITH TIME ZONE,
    dismissed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version INTEGER NOT NULL DEFAULT 0
);

-- Create indexes
CREATE INDEX idx_insights_user_id ON insights(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_insights_type ON insights(insight_type) WHERE deleted_at IS NULL;
CREATE INDEX idx_insights_category ON insights(category) WHERE deleted_at IS NULL;
CREATE INDEX idx_insights_priority ON insights(priority) WHERE deleted_at IS NULL;
CREATE INDEX idx_insights_valid_from ON insights(valid_from) WHERE deleted_at IS NULL;
CREATE INDEX idx_insights_created_at ON insights(created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_insights_user_unread ON insights(user_id, is_read) WHERE deleted_at IS NULL AND is_read = FALSE;
CREATE INDEX idx_insights_metadata ON insights USING gin(metadata) WHERE deleted_at IS NULL;

-- Add comments
COMMENT ON TABLE insights IS 'Stores AI-generated financial insights and recommendations';
COMMENT ON COLUMN insights.insight_type IS 'Type: SPENDING_PATTERN, ANOMALY, RECOMMENDATION, FORECAST, COMPARISON, GOAL_PROGRESS';
COMMENT ON COLUMN insights.priority IS 'Priority: LOW, MEDIUM, HIGH, CRITICAL';
COMMENT ON COLUMN insights.confidence_score IS 'AI confidence score (0-100)';
COMMENT ON COLUMN insights.metadata IS 'Additional insight data in JSON format';

-- rollback DROP TABLE insights;
