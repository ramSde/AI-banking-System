-- Indexes for Fraud Detection Service

-- Fraud Rule Indexes
CREATE INDEX idx_fraud_rule_type ON fraud_rule(rule_type) WHERE deleted_at IS NULL;
CREATE INDEX idx_fraud_rule_enabled ON fraud_rule(enabled) WHERE deleted_at IS NULL;
CREATE INDEX idx_fraud_rule_created_at ON fraud_rule(created_at) WHERE deleted_at IS NULL;

-- Fraud Check Indexes
CREATE INDEX idx_fraud_check_transaction_id ON fraud_check(transaction_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_fraud_check_user_id ON fraud_check(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_fraud_check_risk_level ON fraud_check(risk_level) WHERE deleted_at IS NULL;
CREATE INDEX idx_fraud_check_blocked ON fraud_check(blocked) WHERE deleted_at IS NULL;
CREATE INDEX idx_fraud_check_checked_at ON fraud_check(checked_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_fraud_check_user_checked_at ON fraud_check(user_id, checked_at) WHERE deleted_at IS NULL;

-- Fraud Alert Indexes
CREATE INDEX idx_fraud_alert_fraud_check_id ON fraud_alert(fraud_check_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_fraud_alert_transaction_id ON fraud_alert(transaction_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_fraud_alert_user_id ON fraud_alert(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_fraud_alert_status ON fraud_alert(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_fraud_alert_severity ON fraud_alert(severity) WHERE deleted_at IS NULL;
CREATE INDEX idx_fraud_alert_assigned_to ON fraud_alert(assigned_to) WHERE deleted_at IS NULL;
CREATE INDEX idx_fraud_alert_created_at ON fraud_alert(created_at) WHERE deleted_at IS NULL;

-- Fraud Pattern Indexes
CREATE INDEX idx_fraud_pattern_type ON fraud_pattern(pattern_type) WHERE deleted_at IS NULL;
CREATE INDEX idx_fraud_pattern_last_detected ON fraud_pattern(last_detected_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_fraud_pattern_occurrences ON fraud_pattern(occurrences) WHERE deleted_at IS NULL;

-- Rollback
--rollback DROP INDEX IF EXISTS idx_fraud_rule_type;
--rollback DROP INDEX IF EXISTS idx_fraud_rule_enabled;
--rollback DROP INDEX IF EXISTS idx_fraud_rule_created_at;
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
--rollback DROP INDEX IF EXISTS idx_fraud_pattern_type;
--rollback DROP INDEX IF EXISTS idx_fraud_pattern_last_detected;
--rollback DROP INDEX IF EXISTS idx_fraud_pattern_occurrences;
