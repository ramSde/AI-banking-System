--liquibase formatted sql

--changeset notification-service:1
--comment: Create notification_templates table for storing email, SMS, and push notification templates

CREATE TABLE IF NOT EXISTS notification_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_code VARCHAR(100) NOT NULL UNIQUE,
    template_name VARCHAR(255) NOT NULL,
    channel VARCHAR(50) NOT NULL,
    subject VARCHAR(500),
    body_template TEXT NOT NULL,
    template_variables JSONB,
    locale VARCHAR(10) NOT NULL DEFAULT 'en',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_channel CHECK (channel IN ('EMAIL', 'SMS', 'PUSH'))
);

COMMENT ON TABLE notification_templates IS 'Stores notification templates for email, SMS, and push notifications';
COMMENT ON COLUMN notification_templates.id IS 'Primary key UUID';
COMMENT ON COLUMN notification_templates.template_code IS 'Unique template code (e.g., WELCOME_EMAIL, OTP_SMS)';
COMMENT ON COLUMN notification_templates.template_name IS 'Human-readable template name';
COMMENT ON COLUMN notification_templates.channel IS 'Notification channel: EMAIL, SMS, or PUSH';
COMMENT ON COLUMN notification_templates.subject IS 'Email subject or push notification title';
COMMENT ON COLUMN notification_templates.body_template IS 'Template body with placeholders (Thymeleaf for email)';
COMMENT ON COLUMN notification_templates.template_variables IS 'JSON array of required template variables';
COMMENT ON COLUMN notification_templates.locale IS 'Template locale (e.g., en, es, fr)';
COMMENT ON COLUMN notification_templates.is_active IS 'Whether template is active';
COMMENT ON COLUMN notification_templates.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN notification_templates.updated_at IS 'Record update timestamp';
COMMENT ON COLUMN notification_templates.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN notification_templates.version IS 'Optimistic locking version';

--rollback DROP TABLE IF EXISTS notification_templates;
