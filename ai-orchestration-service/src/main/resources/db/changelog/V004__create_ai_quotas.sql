-- liquibase formatted sql

-- changeset ai-orchestration:4
-- comment: Create ai_quotas table for user token quota tracking

CREATE TABLE IF NOT EXISTS ai_quotas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    user_tier VARCHAR(50) NOT NULL DEFAULT 'FREE',
    daily_token_limit INTEGER NOT NULL DEFAULT 10000,
    monthly_token_limit INTEGER NOT NULL DEFAULT 300000,
    daily_tokens_used INTEGER NOT NULL DEFAULT 0,
    monthly_tokens_used INTEGER NOT NULL DEFAULT 0,
    daily_reset_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    monthly_reset_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE ai_quotas IS 'User AI token quota limits and tracking';
COMMENT ON COLUMN ai_quotas.id IS 'Unique identifier for the quota';
COMMENT ON COLUMN ai_quotas.user_id IS 'User this quota belongs to';
COMMENT ON COLUMN ai_quotas.user_tier IS 'User tier (FREE, BASIC, PREMIUM, ENTERPRISE)';
COMMENT ON COLUMN ai_quotas.daily_token_limit IS 'Daily token limit';
COMMENT ON COLUMN ai_quotas.monthly_token_limit IS 'Monthly token limit';
COMMENT ON COLUMN ai_quotas.daily_tokens_used IS 'Tokens used today';
COMMENT ON COLUMN ai_quotas.monthly_tokens_used IS 'Tokens used this month';
COMMENT ON COLUMN ai_quotas.daily_reset_at IS 'When daily quota resets';
COMMENT ON COLUMN ai_quotas.monthly_reset_at IS 'When monthly quota resets';
COMMENT ON COLUMN ai_quotas.created_at IS 'Timestamp when record was created';
COMMENT ON COLUMN ai_quotas.updated_at IS 'Timestamp when record was last updated';
COMMENT ON COLUMN ai_quotas.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN ai_quotas.version IS 'Optimistic locking version';

-- rollback DROP TABLE IF EXISTS ai_quotas;
