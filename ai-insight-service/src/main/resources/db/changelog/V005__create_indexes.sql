-- liquibase formatted sql

-- changeset ai-insight-service:5
-- comment: Create additional composite indexes for performance optimization

-- Composite indexes for insights
CREATE INDEX idx_insights_user_type_priority ON insights(user_id, insight_type, priority) WHERE deleted_at IS NULL;
CREATE INDEX idx_insights_user_created ON insights(user_id, created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_insights_type_valid ON insights(insight_type, valid_from, valid_until) WHERE deleted_at IS NULL;

-- Composite indexes for spending_patterns
CREATE INDEX idx_spending_patterns_user_category ON spending_patterns(user_id, category) WHERE deleted_at IS NULL;
CREATE INDEX idx_spending_patterns_user_recurring ON spending_patterns(user_id, is_recurring) WHERE deleted_at IS NULL AND is_recurring = TRUE;
CREATE INDEX idx_spending_patterns_category_frequency ON spending_patterns(category, frequency) WHERE deleted_at IS NULL;

-- Composite indexes for recommendations
CREATE INDEX idx_recommendations_user_status_priority ON recommendations(user_id, status, priority) WHERE deleted_at IS NULL;
CREATE INDEX idx_recommendations_user_type ON recommendations(user_id, recommendation_type) WHERE deleted_at IS NULL;
CREATE INDEX idx_recommendations_status_expires ON recommendations(status, expires_at) WHERE deleted_at IS NULL;

-- Composite indexes for anomalies
CREATE INDEX idx_anomalies_user_severity ON anomalies(user_id, severity) WHERE deleted_at IS NULL;
CREATE INDEX idx_anomalies_user_type ON anomalies(user_id, anomaly_type) WHERE deleted_at IS NULL;
CREATE INDEX idx_anomalies_type_detected ON anomalies(anomaly_type, detected_at DESC) WHERE deleted_at IS NULL;

-- Partial indexes for common queries
CREATE INDEX idx_insights_active ON insights(user_id, valid_from, valid_until) 
    WHERE deleted_at IS NULL AND is_dismissed = FALSE AND valid_until > CURRENT_TIMESTAMP;

CREATE INDEX idx_recommendations_actionable ON recommendations(user_id, priority, created_at DESC) 
    WHERE deleted_at IS NULL AND status = 'PENDING' AND (expires_at IS NULL OR expires_at > CURRENT_TIMESTAMP);

CREATE INDEX idx_anomalies_recent_critical ON anomalies(user_id, detected_at DESC) 
    WHERE deleted_at IS NULL AND severity IN ('HIGH', 'CRITICAL') AND is_acknowledged = FALSE;

-- Add statistics for query optimization
ANALYZE insights;
ANALYZE spending_patterns;
ANALYZE recommendations;
ANALYZE anomalies;

-- rollback DROP INDEX IF EXISTS idx_insights_user_type_priority;
-- rollback DROP INDEX IF EXISTS idx_insights_user_created;
-- rollback DROP INDEX IF EXISTS idx_insights_type_valid;
-- rollback DROP INDEX IF EXISTS idx_spending_patterns_user_category;
-- rollback DROP INDEX IF EXISTS idx_spending_patterns_user_recurring;
-- rollback DROP INDEX IF EXISTS idx_spending_patterns_category_frequency;
-- rollback DROP INDEX IF EXISTS idx_recommendations_user_status_priority;
-- rollback DROP INDEX IF EXISTS idx_recommendations_user_type;
-- rollback DROP INDEX IF EXISTS idx_recommendations_status_expires;
-- rollback DROP INDEX IF EXISTS idx_anomalies_user_severity;
-- rollback DROP INDEX IF EXISTS idx_anomalies_user_type;
-- rollback DROP INDEX IF EXISTS idx_anomalies_type_detected;
-- rollback DROP INDEX IF EXISTS idx_insights_active;
-- rollback DROP INDEX IF EXISTS idx_recommendations_actionable;
-- rollback DROP INDEX IF EXISTS idx_anomalies_recent_critical;
