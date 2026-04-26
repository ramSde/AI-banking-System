-- liquibase formatted sql

-- changeset chat:4
-- comment: Create indexes for performance optimization

-- Indexes on chat_sessions
CREATE INDEX idx_chat_sessions_user_id ON chat_sessions(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_chat_sessions_status ON chat_sessions(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_chat_sessions_created_at ON chat_sessions(created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_chat_sessions_last_message_at ON chat_sessions(last_message_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_chat_sessions_expires_at ON chat_sessions(expires_at) WHERE deleted_at IS NULL AND status = 'ACTIVE';

-- Composite indexes for common query patterns
CREATE INDEX idx_chat_sessions_user_status ON chat_sessions(user_id, status, created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_chat_sessions_user_last_message ON chat_sessions(user_id, last_message_at DESC) WHERE deleted_at IS NULL;

-- Indexes on chat_messages
CREATE INDEX idx_chat_messages_session_id ON chat_messages(session_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_chat_messages_user_id ON chat_messages(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_chat_messages_role ON chat_messages(role) WHERE deleted_at IS NULL;
CREATE INDEX idx_chat_messages_created_at ON chat_messages(created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_chat_messages_rag_query_id ON chat_messages(rag_query_id) WHERE deleted_at IS NULL AND rag_query_id IS NOT NULL;

-- Composite indexes for message queries
CREATE INDEX idx_chat_messages_session_created ON chat_messages(session_id, created_at ASC) WHERE deleted_at IS NULL;
CREATE INDEX idx_chat_messages_user_created ON chat_messages(user_id, created_at DESC) WHERE deleted_at IS NULL;

-- GIN index on sources JSONB
CREATE INDEX idx_chat_messages_sources ON chat_messages USING GIN (sources) WHERE deleted_at IS NULL;

-- Indexes on message_feedback
CREATE INDEX idx_message_feedback_message_id ON message_feedback(message_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_message_feedback_user_id ON message_feedback(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_message_feedback_rating ON message_feedback(rating) WHERE deleted_at IS NULL;
CREATE INDEX idx_message_feedback_feedback_type ON message_feedback(feedback_type) WHERE deleted_at IS NULL;
CREATE INDEX idx_message_feedback_created_at ON message_feedback(created_at DESC) WHERE deleted_at IS NULL;

-- rollback DROP INDEX IF EXISTS idx_chat_sessions_user_id;
-- rollback DROP INDEX IF EXISTS idx_chat_sessions_status;
-- rollback DROP INDEX IF EXISTS idx_chat_sessions_created_at;
-- rollback DROP INDEX IF EXISTS idx_chat_sessions_last_message_at;
-- rollback DROP INDEX IF EXISTS idx_chat_sessions_expires_at;
-- rollback DROP INDEX IF EXISTS idx_chat_sessions_user_status;
-- rollback DROP INDEX IF EXISTS idx_chat_sessions_user_last_message;
-- rollback DROP INDEX IF EXISTS idx_chat_messages_session_id;
-- rollback DROP INDEX IF EXISTS idx_chat_messages_user_id;
-- rollback DROP INDEX IF EXISTS idx_chat_messages_role;
-- rollback DROP INDEX IF EXISTS idx_chat_messages_created_at;
-- rollback DROP INDEX IF EXISTS idx_chat_messages_rag_query_id;
-- rollback DROP INDEX IF EXISTS idx_chat_messages_session_created;
-- rollback DROP INDEX IF EXISTS idx_chat_messages_user_created;
-- rollback DROP INDEX IF EXISTS idx_chat_messages_sources;
-- rollback DROP INDEX IF EXISTS idx_message_feedback_message_id;
-- rollback DROP INDEX IF EXISTS idx_message_feedback_user_id;
-- rollback DROP INDEX IF EXISTS idx_message_feedback_rating;
-- rollback DROP INDEX IF EXISTS idx_message_feedback_feedback_type;
-- rollback DROP INDEX IF EXISTS idx_message_feedback_created_at;
