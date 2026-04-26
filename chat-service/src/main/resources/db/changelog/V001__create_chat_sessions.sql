-- liquibase formatted sql

-- changeset chat:1
-- comment: Create chat_sessions table for managing chat sessions

CREATE TABLE chat_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    title VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    context_summary TEXT,
    message_count INTEGER NOT NULL DEFAULT 0,
    last_message_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_session_status CHECK (status IN ('ACTIVE', 'ENDED', 'EXPIRED', 'ARCHIVED'))
);

COMMENT ON TABLE chat_sessions IS 'Stores chat session information for multi-turn conversations';
COMMENT ON COLUMN chat_sessions.id IS 'Primary key';
COMMENT ON COLUMN chat_sessions.user_id IS 'User who owns this session';
COMMENT ON COLUMN chat_sessions.title IS 'Session title (auto-generated or user-provided)';
COMMENT ON COLUMN chat_sessions.status IS 'Session status: ACTIVE, ENDED, EXPIRED, ARCHIVED';
COMMENT ON COLUMN chat_sessions.context_summary IS 'Summary of conversation context';
COMMENT ON COLUMN chat_sessions.message_count IS 'Total number of messages in session';
COMMENT ON COLUMN chat_sessions.last_message_at IS 'Timestamp of last message';
COMMENT ON COLUMN chat_sessions.expires_at IS 'Session expiration timestamp';
COMMENT ON COLUMN chat_sessions.metadata IS 'Additional session metadata (tags, preferences)';
COMMENT ON COLUMN chat_sessions.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN chat_sessions.updated_at IS 'Record last update timestamp';
COMMENT ON COLUMN chat_sessions.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN chat_sessions.version IS 'Optimistic locking version';

-- rollback DROP TABLE chat_sessions;
