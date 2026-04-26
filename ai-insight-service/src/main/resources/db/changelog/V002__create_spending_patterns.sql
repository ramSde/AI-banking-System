-- liquibase formatted sql

-- changeset ai-insight-service:2
-- comment: Create spending_patterns table for tracking identified spending patterns

CREATE TABLE spending_patterns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    pattern_type VARCHAR(50) NOT NULL,
    category VARCHAR(100) NOT NULL,
    merchant_name VARCHAR(255),
    frequency VARCHAR(20) NOT NULL,
    average_amount DECIMAL(15,2) NOT NULL,
    min_amount DECIMAL(15,2) NOT NULL,
    max_amount DECIMAL(15,2) NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    transaction_count INTEGER NOT NULL,
    first_occurrence TIMESTAMP WITH TIME ZONE NOT NULL,
    last_occurrence TIMESTAMP WITH TIME ZONE NOT NULL,
    next_predicted_date TIMESTAMP WITH TIME ZONE,
    confidence_score DECIMAL(5,2) NOT NULL,
    is_recurring BOOLEAN NOT NULL DEFAULT FALSE,
    is_seasonal BOOLEAN NOT NULL DEFAULT FALSE,
    season VARCHAR(20),
    trend VARCHAR(20),
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version INTEGER NOT NULL DEFAULT 0
);

-- Create indexes
CREATE INDEX idx_spending_patterns_user_id ON spending_patterns(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_spending_patterns_type ON spending_patterns(pattern_type) WHERE deleted_at IS NULL;
CREATE INDEX idx_spending_patterns_category ON spending_patterns(category) WHERE deleted_at IS NULL;
CREATE INDEX idx_spending_patterns_merchant ON spending_patterns(merchant_name) WHERE deleted_at IS NULL;
CREATE INDEX idx_spending_patterns_frequency ON spending_patterns(frequency) WHERE deleted_at IS NULL;
CREATE INDEX idx_spending_patterns_recurring ON spending_patterns(is_recurring) WHERE deleted_at IS NULL AND is_recurring = TRUE;
CREATE INDEX idx_spending_patterns_next_predicted ON spending_patterns(next_predicted_date) WHERE deleted_at IS NULL;
CREATE INDEX idx_spending_patterns_metadata ON spending_patterns USING gin(metadata) WHERE deleted_at IS NULL;

-- Add comments
COMMENT ON TABLE spending_patterns IS 'Tracks identified spending patterns and trends';
COMMENT ON COLUMN spending_patterns.pattern_type IS 'Type: RECURRING, SEASONAL, TRENDING_UP, TRENDING_DOWN, STABLE';
COMMENT ON COLUMN spending_patterns.frequency IS 'Frequency: DAILY, WEEKLY, BIWEEKLY, MONTHLY, QUARTERLY, YEARLY';
COMMENT ON COLUMN spending_patterns.trend IS 'Trend: INCREASING, DECREASING, STABLE';
COMMENT ON COLUMN spending_patterns.season IS 'Season: SPRING, SUMMER, FALL, WINTER';

-- rollback DROP TABLE spending_patterns;
