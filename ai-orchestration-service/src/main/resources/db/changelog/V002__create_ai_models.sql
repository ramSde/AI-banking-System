-- liquibase formatted sql

-- changeset ai-orchestration:2
-- comment: Create ai_models table for AI model configuration

CREATE TABLE IF NOT EXISTS ai_models (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    provider VARCHAR(50) NOT NULL,
    model_type VARCHAR(50) NOT NULL,
    input_price_per_1k DECIMAL(10,6) NOT NULL DEFAULT 0.0,
    output_price_per_1k DECIMAL(10,6) NOT NULL DEFAULT 0.0,
    max_tokens INTEGER NOT NULL DEFAULT 4096,
    context_window INTEGER NOT NULL DEFAULT 8192,
    enabled BOOLEAN NOT NULL DEFAULT true,
    priority INTEGER NOT NULL DEFAULT 0,
    capabilities JSONB,
    configuration JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE ai_models IS 'Configuration for available AI models';
COMMENT ON COLUMN ai_models.id IS 'Unique identifier for the model';
COMMENT ON COLUMN ai_models.name IS 'Model name (e.g., gpt-4, claude-3-sonnet)';
COMMENT ON COLUMN ai_models.provider IS 'AI provider (openai, anthropic, ollama)';
COMMENT ON COLUMN ai_models.model_type IS 'Model type (chat, completion, embedding)';
COMMENT ON COLUMN ai_models.input_price_per_1k IS 'Cost per 1K input tokens in USD';
COMMENT ON COLUMN ai_models.output_price_per_1k IS 'Cost per 1K output tokens in USD';
COMMENT ON COLUMN ai_models.max_tokens IS 'Maximum tokens per request';
COMMENT ON COLUMN ai_models.context_window IS 'Maximum context window size';
COMMENT ON COLUMN ai_models.enabled IS 'Whether model is currently enabled';
COMMENT ON COLUMN ai_models.priority IS 'Model priority (higher = preferred)';
COMMENT ON COLUMN ai_models.capabilities IS 'Model capabilities (JSON)';
COMMENT ON COLUMN ai_models.configuration IS 'Model-specific configuration (JSON)';
COMMENT ON COLUMN ai_models.created_at IS 'Timestamp when record was created';
COMMENT ON COLUMN ai_models.updated_at IS 'Timestamp when record was last updated';
COMMENT ON COLUMN ai_models.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN ai_models.version IS 'Optimistic locking version';

-- Insert default models
INSERT INTO ai_models (name, provider, model_type, input_price_per_1k, output_price_per_1k, max_tokens, context_window, enabled, priority)
VALUES 
    ('gpt-4', 'openai', 'chat', 0.03, 0.06, 4096, 8192, true, 100),
    ('gpt-3.5-turbo', 'openai', 'chat', 0.0015, 0.002, 4096, 16384, true, 80),
    ('claude-3-sonnet-20240229', 'anthropic', 'chat', 0.008, 0.024, 4096, 200000, true, 90),
    ('llama2', 'ollama', 'chat', 0.0, 0.0, 2048, 4096, true, 50);

-- rollback DROP TABLE IF EXISTS ai_models;
