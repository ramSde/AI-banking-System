--liquibase formatted sql

--changeset user-service:2
--comment: Create user_preference table for user settings and preferences

CREATE TABLE IF NOT EXISTS user_preference (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    language VARCHAR(10) NOT NULL DEFAULT 'en',
    timezone VARCHAR(50) NOT NULL DEFAULT 'UTC',
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    date_format VARCHAR(20) NOT NULL DEFAULT 'YYYY-MM-DD',
    time_format VARCHAR(10) NOT NULL DEFAULT '24H',
    notification_email BOOLEAN NOT NULL DEFAULT TRUE,
    notification_sms BOOLEAN NOT NULL DEFAULT TRUE,
    notification_push BOOLEAN NOT NULL DEFAULT TRUE,
    notification_transaction BOOLEAN NOT NULL DEFAULT TRUE,
    notification_login BOOLEAN NOT NULL DEFAULT TRUE,
    notification_marketing BOOLEAN NOT NULL DEFAULT FALSE,
    two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    biometric_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    session_timeout_minutes INTEGER NOT NULL DEFAULT 30,
    theme VARCHAR(20) NOT NULL DEFAULT 'LIGHT',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_user_preference_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT chk_language CHECK (language IN ('en', 'es', 'fr', 'de', 'hi', 'ar', 'zh', 'ja')),
    CONSTRAINT chk_currency CHECK (currency ~ '^[A-Z]{3}$'),
    CONSTRAINT chk_timezone CHECK (length(timezone) > 0),
    CONSTRAINT chk_session_timeout CHECK (session_timeout_minutes BETWEEN 5 AND 120),
    CONSTRAINT chk_theme CHECK (theme IN ('LIGHT', 'DARK', 'AUTO'))
);

COMMENT ON TABLE user_preference IS 'User preferences and notification settings';
COMMENT ON COLUMN user_preference.id IS 'Unique preference identifier';
COMMENT ON COLUMN user_preference.user_id IS 'Reference to user table';
COMMENT ON COLUMN user_preference.language IS 'Preferred language code (ISO 639-1)';
COMMENT ON COLUMN user_preference.timezone IS 'User timezone (IANA timezone database)';
COMMENT ON COLUMN user_preference.currency IS 'Preferred currency code (ISO 4217)';
COMMENT ON COLUMN user_preference.date_format IS 'Preferred date format';
COMMENT ON COLUMN user_preference.time_format IS 'Preferred time format (12H or 24H)';
COMMENT ON COLUMN user_preference.notification_email IS 'Enable email notifications';
COMMENT ON COLUMN user_preference.notification_sms IS 'Enable SMS notifications';
COMMENT ON COLUMN user_preference.notification_push IS 'Enable push notifications';
COMMENT ON COLUMN user_preference.notification_transaction IS 'Enable transaction notifications';
COMMENT ON COLUMN user_preference.notification_login IS 'Enable login notifications';
COMMENT ON COLUMN user_preference.notification_marketing IS 'Enable marketing notifications';
COMMENT ON COLUMN user_preference.two_factor_enabled IS 'Two-factor authentication enabled';
COMMENT ON COLUMN user_preference.biometric_enabled IS 'Biometric authentication enabled';
COMMENT ON COLUMN user_preference.session_timeout_minutes IS 'Session timeout in minutes';
COMMENT ON COLUMN user_preference.theme IS 'UI theme preference';
COMMENT ON COLUMN user_preference.version IS 'Optimistic locking version';

-- Indexes
CREATE UNIQUE INDEX idx_user_preference_user_id ON user_preference(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_user_preference_language ON user_preference(language) WHERE deleted_at IS NULL;
CREATE INDEX idx_user_preference_deleted_at ON user_preference(deleted_at) WHERE deleted_at IS NOT NULL;

--rollback DROP TABLE IF EXISTS user_preference;
