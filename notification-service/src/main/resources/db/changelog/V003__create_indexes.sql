--liquibase formatted sql

--changeset notification-service:3
--comment: Create indexes for notification_templates and notification_history tables

-- Indexes for notification_templates
CREATE INDEX IF NOT EXISTS idx_notification_templates_code ON notification_templates(template_code);
CREATE INDEX IF NOT EXISTS idx_notification_templates_channel ON notification_templates(channel);
CREATE INDEX IF NOT EXISTS idx_notification_templates_active ON notification_templates(is_active) WHERE is_active = true;
CREATE INDEX IF NOT EXISTS idx_notification_templates_locale ON notification_templates(locale);

-- Indexes for notification_history
CREATE INDEX IF NOT EXISTS idx_notification_history_notification_id ON notification_history(notification_id);
CREATE INDEX IF NOT EXISTS idx_notification_history_user_id ON notification_history(user_id);
CREATE INDEX IF NOT EXISTS idx_notification_history_channel ON notification_history(channel);
CREATE INDEX IF NOT EXISTS idx_notification_history_status ON notification_history(status);
CREATE INDEX IF NOT EXISTS idx_notification_history_template_code ON notification_history(template_code);
CREATE INDEX IF NOT EXISTS idx_notification_history_created_at ON notification_history(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notification_history_sent_at ON notification_history(sent_at DESC) WHERE sent_at IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_notification_history_trace_id ON notification_history(trace_id) WHERE trace_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_notification_history_correlation_id ON notification_history(correlation_id) WHERE correlation_id IS NOT NULL;

-- Composite indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_notification_history_user_channel ON notification_history(user_id, channel, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notification_history_user_status ON notification_history(user_id, status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notification_history_channel_status ON notification_history(channel, status, created_at DESC);

COMMENT ON INDEX idx_notification_templates_code IS 'Index for template code lookups';
COMMENT ON INDEX idx_notification_templates_channel IS 'Index for filtering by channel';
COMMENT ON INDEX idx_notification_templates_active IS 'Partial index for active templates';
COMMENT ON INDEX idx_notification_templates_locale IS 'Index for filtering by locale';
COMMENT ON INDEX idx_notification_history_notification_id IS 'Index for notification ID lookups';
COMMENT ON INDEX idx_notification_history_user_id IS 'Index for user-specific queries';
COMMENT ON INDEX idx_notification_history_channel IS 'Index for filtering by channel';
COMMENT ON INDEX idx_notification_history_status IS 'Index for filtering by status';
COMMENT ON INDEX idx_notification_history_template_code IS 'Index for template-specific queries';
COMMENT ON INDEX idx_notification_history_created_at IS 'Index for time-based queries';
COMMENT ON INDEX idx_notification_history_sent_at IS 'Partial index for sent notifications';
COMMENT ON INDEX idx_notification_history_trace_id IS 'Partial index for distributed tracing';
COMMENT ON INDEX idx_notification_history_correlation_id IS 'Partial index for correlation tracking';
COMMENT ON INDEX idx_notification_history_user_channel IS 'Composite index for user + channel queries';
COMMENT ON INDEX idx_notification_history_user_status IS 'Composite index for user + status queries';
COMMENT ON INDEX idx_notification_history_channel_status IS 'Composite index for channel + status queries';

--rollback DROP INDEX IF EXISTS idx_notification_templates_code;
--rollback DROP INDEX IF EXISTS idx_notification_templates_channel;
--rollback DROP INDEX IF EXISTS idx_notification_templates_active;
--rollback DROP INDEX IF EXISTS idx_notification_templates_locale;
--rollback DROP INDEX IF EXISTS idx_notification_history_notification_id;
--rollback DROP INDEX IF EXISTS idx_notification_history_user_id;
--rollback DROP INDEX IF EXISTS idx_notification_history_channel;
--rollback DROP INDEX IF EXISTS idx_notification_history_status;
--rollback DROP INDEX IF EXISTS idx_notification_history_template_code;
--rollback DROP INDEX IF EXISTS idx_notification_history_created_at;
--rollback DROP INDEX IF EXISTS idx_notification_history_sent_at;
--rollback DROP INDEX IF EXISTS idx_notification_history_trace_id;
--rollback DROP INDEX IF EXISTS idx_notification_history_correlation_id;
--rollback DROP INDEX IF EXISTS idx_notification_history_user_channel;
--rollback DROP INDEX IF EXISTS idx_notification_history_user_status;
--rollback DROP INDEX IF EXISTS idx_notification_history_channel_status;
