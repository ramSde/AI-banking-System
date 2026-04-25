--liquibase formatted sql

--changeset notification-service:2
--comment: Create notification_history table for tracking all sent notifications

CREATE TABLE IF NOT EXISTS notification_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_id VARCHAR(255) NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    recipient_email VARCHAR(255),
    recipient_phone VARCHAR(20),
    recipient_device_token VARCHAR(500),
    channel VARCHAR(50) NOT NULL,
    template_code VARCHAR(100) NOT NULL,
    subject VARCHAR(500),
    body TEXT NOT NULL,
    template_variables JSONB,
    status VARCHAR(50) NOT NULL,
    provider_response TEXT,
    error_message TEXT,
    retry_count INT NOT NULL DEFAULT 0,
    sent_at TIMESTAMPTZ,
    delivered_at TIMESTAMPTZ,
    failed_at TIMESTAMPTZ,
    correlation_id VARCHAR(255),
    trace_id VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_channel CHECK (channel IN ('EMAIL', 'SMS', 'PUSH')),
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'SENT', 'DELIVERED', 'FAILED', 'RETRYING'))
);

COMMENT ON TABLE notification_history IS 'Complete history of all notifications sent through the system';
COMMENT ON COLUMN notification_history.id IS 'Primary key UUID';
COMMENT ON COLUMN notification_history.notification_id IS 'Unique notification identifier';
COMMENT ON COLUMN notification_history.user_id IS 'User ID who received the notification';
COMMENT ON COLUMN notification_history.recipient_email IS 'Email address (for EMAIL channel)';
COMMENT ON COLUMN notification_history.recipient_phone IS 'Phone number (for SMS channel)';
COMMENT ON COLUMN notification_history.recipient_device_token IS 'Device token (for PUSH channel)';
COMMENT ON COLUMN notification_history.channel IS 'Notification channel: EMAIL, SMS, or PUSH';
COMMENT ON COLUMN notification_history.template_code IS 'Template code used';
COMMENT ON COLUMN notification_history.subject IS 'Email subject or push notification title';
COMMENT ON COLUMN notification_history.body IS 'Rendered notification body';
COMMENT ON COLUMN notification_history.template_variables IS 'Variables used to render template';
COMMENT ON COLUMN notification_history.status IS 'Notification status: PENDING, SENT, DELIVERED, FAILED, RETRYING';
COMMENT ON COLUMN notification_history.provider_response IS 'Response from email/SMS/push provider';
COMMENT ON COLUMN notification_history.error_message IS 'Error message if failed';
COMMENT ON COLUMN notification_history.retry_count IS 'Number of retry attempts';
COMMENT ON COLUMN notification_history.sent_at IS 'Timestamp when notification was sent';
COMMENT ON COLUMN notification_history.delivered_at IS 'Timestamp when notification was delivered';
COMMENT ON COLUMN notification_history.failed_at IS 'Timestamp when notification failed';
COMMENT ON COLUMN notification_history.correlation_id IS 'Correlation ID for tracking related events';
COMMENT ON COLUMN notification_history.trace_id IS 'Distributed tracing trace ID';
COMMENT ON COLUMN notification_history.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN notification_history.updated_at IS 'Record update timestamp';
COMMENT ON COLUMN notification_history.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN notification_history.version IS 'Optimistic locking version';

--rollback DROP TABLE IF EXISTS notification_history;
