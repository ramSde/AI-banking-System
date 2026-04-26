-- liquibase formatted sql

-- changeset chat:2
-- comment: Create chat_messages table for storing conversation messages

CREATE TABLE chat_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    model_name VARCHAR(100),
    input_tokens INTEGER,
    output_tokens INTEGER,
    total_tokens INTEGER,
    latency_ms BIGINT,
    rag_context_used BOOLEAN DEFAULT false,
    rag_query_id UUID,
    sources JSONB,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_chat_messages_session FOREIGN KEY (session_id) REFERENCES chat_sessions(id) ON DELETE CASCADE,
    CONSTRAINT chk_message_role CHECK (role IN ('USER', 'ASSISTANT', 'SYSTEM'))
);

COMMENT ON TABLE chat_messages IS 'Stores individual chat messages within sessions';
COMMENT ON COLUMN chat_messages.id IS 'Primary key';
COMMENT ON COLUMN chat_messages.session_id IS 'Reference to chat session';
COMMENT ON COLUMN chat_messages.user_id IS 'User who sent/received the message';
COMMENT ON COLUMN chat_messages.role IS 'Message role: USER, ASSISTANT, SYSTEM';
COMMENT ON COLUMN chat_messages.content IS 'Message content';
COMMENT ON COLUMN chat_messages.model_name IS 'AI model used for assistant messages';
COMMENT ON COLUMN chat_messages.input_tokens IS 'Input tokens consumed';
COMMENT ON COLUMN chat_messages.output_tokens IS 'Output tokens generated';
COMMENT ON COLUMN chat_messages.total_tokens IS 'Total tokens used';
COMMENT ON COLUMN chat_messages.latency_ms IS 'Response latency in milliseconds';
COMMENT ON COLUMN chat_messages.rag_context_used IS 'Whether RAG context was used';
COMMENT ON COLUMN chat_messages.rag_query_id IS 'Reference to RAG query if used';
COMMENT ON COLUMN chat_messages.sources IS 'Document sources used in response';
COMMENT ON COLUMN chat_messages.metadata IS 'Additional message metadata';
COMMENT ON COLUMN chat_messages.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN chat_messages.updated_at IS 'Record last update timestamp';
COMMENT ON COLUMN chat_messages.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN chat_messages.version IS 'Optimistic locking version';

-- rollback DROP TABLE chat_messages;
