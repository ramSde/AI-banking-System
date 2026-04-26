-- liquibase formatted sql

-- changeset ai-orchestration:1
-- comment: Create ai_usage table for tracking all AI API calls

CREATE TABLE IF NOT EXISTS ai_usage (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    session_id VARCHAR(255),
    model_name VARCHAR(100) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    feature VARCHAR(50) NOT NULL,
    input_tokens INTEGER NOT NULL DEFAULT 0,
    output_tokens INTEGER NOT NULL DEFAULT 0,
    total_tokens INTEGER NOT NULL DEFAULT 0,
    latency_ms BIGINT NOT NULL,
    cost_usd DECIMAL(10,6) NOT NULL DEFAULT 0.0,
    success BOOLEAN NOT NULL DEFAULT true,
    error_message TEXT,
    trace_id VARCHAR(255),
    request_payload JSONB,
    response_payload JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE ai_usage IS 'Tracks every AI API call with complete metrics';
COMMENT ON COLUMN ai_usage.id IS 'Unique identifier for the AI usage record';
COMMENT ON COLUMN ai_usage.user_id IS 'User who made the AI request';
COMMENT ON COLUMN ai_usage.session_id IS 'Session identifier for grouping related requests';
COMMENT ON COLUMN ai_usage.model_name IS 'AI model used (e.g., gpt-4, claude-3)';
COMMENT ON COLUMN ai_usage.provider IS 'AI provider (openai, anthropic, ollama)';
COMMENT ON COLUMN ai_usage.feature IS 'Feature using AI (RAG, INSIGHT, CHAT, etc.)';
COMMENT ON COLUMN ai_usage.input_tokens IS 'Number of input tokens consumed';
COMMENT ON COLUMN ai_usage.output_tokens IS 'Number of output tokens generated';
COMMENT ON COLUMN ai_usage.total_tokens IS 'Total tokens (input + output)';
COMMENT ON COLUMN ai_usage.latency_ms IS 'Request latency in milliseconds';
COMMENT ON COLUMN ai_usage.cost_usd IS 'Calculated cost in USD';
COMMENT ON COLUMN ai_usage.success IS 'Whether the request succeeded';
COMMENT ON COLUMN ai_usage.error_message IS 'Error message if request failed';
COMMENT ON COLUMN ai_usage.trace_id IS 'Distributed tracing ID';
COMMENT ON COLUMN ai_usage.request_payload IS 'Request payload (for debugging)';
COMMENT ON COLUMN ai_usage.response_payload IS 'Response payload (for debugging)';
COMMENT ON COLUMN ai_usage.created_at IS 'Timestamp when record was created';
COMMENT ON COLUMN ai_usage.updated_at IS 'Timestamp when record was last updated';
COMMENT ON COLUMN ai_usage.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN ai_usage.version IS 'Optimistic locking version';

-- rollback DROP TABLE IF EXISTS ai_usage;
