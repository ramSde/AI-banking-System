--liquibase formatted sql

--changeset fraud-detection:5
--comment: Create indexes for fraud detection tables

-- Fraud Rule Indexes
CREATE INDEX idx_fraud_rule_type ON fraud_rule(rule_type);
CREATE INDEX idx_fraud_rule_enabled ON fraud_rule(enabled) WHERE deleted_at IS NULL;
CREATE INDEX idx_fraud_rule_created_by ON fraud_rule(created_by);

-- Fraud Check Indexes
CREATE INDEX idx_fraud_check_transaction_id ON fraud_check(transaction_id);
CREATE INDEX idx_fraud_check_user_id ON fraud_check(user_id);
CREATE INDEX idx_fraud_check_risk_level ON fraud_check(risk_level);
CREATE INDEX idx_fraud_check_blocked ON fraud_check(blocked);
CREATE INDEX idx_fraud_check_checked_at ON fraud_check(checked_at DESC);
CREATE INDEX idx_fraud_check_user_checked_at ON fraud_check(user_id, checked_at DESC);

-- Fraud Alert Indexes
CREATE INDEX idx_fraud_alert_fraud_check_id ON fraud_alert(fraud_check_id);
CREATE INDEX idx_fraud_alert_transaction_id ON fraud_alert(transaction_id);
CREATE INDEX idx_fraud_alert_user_id ON fraud_alert(user_id);
CREATE INDEX idx_fraud_alert_status ON fraud_alert(status);
CREATE INDEX idx_fraud_alert_severity ON fraud_alert(severity);
CREATE INDEX idx_fraud_alert_assigned_to ON fraud_alert(assigned_to);
CREATE INDEX idx_fraud_alert_created_at ON fraud_alert(created_at DESC);
CREATE INDEX idx_fraud_alert_status_created_at ON fraud_alert(status, created_at DESC);

-- Fraud Pattern Indexes
CREATE INDEX idx_fraud_pattern_type ON fraud_pattern(pattern_type);
CREATE INDEX idx_fraud_pattern_user_id ON fraud_pattern(user_id);
CREATE INDEX idx_fraud_pattern_severity ON fraud_pattern(severity);
CREATE INDEX idx_fraud_pattern_active ON fraud_pattern(active);
CREATE INDEX idx_fraud_pattern_last_detected ON fraud_pattern(last_detected_at DESC);
CREATE INDEX idx_fraud_pattern_user_active ON fraud_pattern(user_id, active) WHERE user_id IS NOT NULL;

--rollback DROP INDEX IF EXISTS idx_fraud_rule_type;
--rollback DROP INDEX IF EXISTS idx_fraud_rule_enabled;
--rollback DROP INDEX IF EXISTS idx_fraud_rule_created_by;
--rollback DROP INDEX IF EXISTS idx_fraud_check_transaction_id;
--rollback DROP INDEX IF EXISTS idx_fraud_check_user_id;
--rollback DROP INDEX IF EXISTS idx_fraud_check_risk_level;
--rollback DROP INDEX IF EXISTS idx_fraud_check_blocked;
--rollback DROP INDEX IF EXISTS idx_fraud_check_checked_at;
--rollback DROP INDEX IF EXISTS idx_fraud_check_user_checked_at;
--rollback DROP INDEX IF EXISTS idx_fraud_alert_fraud_check_id;
--rollback DROP INDEX IF EXISTS idx_fraud_alert_transaction_id;
--rollback DROP INDEX IF EXISTS idx_fraud_alert_user_id;
--rollback DROP INDEX IF EXISTS idx_fraud_alert_status;
--rollback DROP INDEX IF EXISTS idx_fraud_alert_severity;
--rollback DROP INDEX IF EXISTS idx_fraud_alert_assigned_to;
--rollback DROP INDEX IF EXISTS idx_fraud_alert_created_at;
--rollback DROP INDEX IF EXISTS idx_fraud_alert_status_created_at;
--rollback DROP INDEX IF EXISTS idx_fraud_pattern_type;
--rollback DROP INDEX IF EXISTS idx_fraud_pattern_user_id;
--rollback DROP INDEX IF EXISTS idx_fraud_pattern_severity;
--rollback DROP INDEX IF EXISTS idx_fraud_pattern_active;
--rollback DROP INDEX IF EXISTS idx_fraud_pattern_last_detected;
--rollback DROP INDEX IF EXISTS idx_fraud_pattern_user_active;
