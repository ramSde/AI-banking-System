-- liquibase formatted sql

-- changeset ai-orchestration:5
-- comment: Create indexes for performance optimization

-- Indexes for ai_usage table
CREATE INDEX IF NOT EXISTS idx_ai_usage_user_id ON ai_usage(user_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_ai_usage_created_at ON ai_usage(created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_ai_usage_user_created ON ai_usage(user_id, created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_ai_usage_session_id ON ai_usage(session_id) WHERE deleted_at IS NULL AND session_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_ai_usage_trace_id ON ai_usage(trace_id) WHERE deleted_at IS NULL AND trace_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_ai_usage_provider ON ai_usage(provider) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_ai_usage_model_name ON ai_usage(model_name) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_ai_usage_feature ON ai_usage(feature) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_ai_usage_success ON ai_usage(success) WHERE deleted_at IS NULL;

-- Indexes for ai_models table
CREATE INDEX IF NOT EXISTS idx_ai_models_provider ON ai_models(provider) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_ai_models_enabled ON ai_models(enabled) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_ai_models_priority ON ai_models(priority DESC) WHERE deleted_at IS NULL AND enabled = true;

-- Indexes for ai_budgets table
CREATE INDEX IF NOT EXISTS idx_ai_budgets_user_id ON ai_budgets(user_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_ai_budgets_daily_reset ON ai_budgets(daily_reset_at) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_ai_budgets_monthly_reset ON ai_budgets(monthly_reset_at) WHERE deleted_at IS NULL;

-- Indexes for ai_quotas table
CREATE INDEX IF NOT EXISTS idx_ai_quotas_user_id ON ai_quotas(user_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_ai_quotas_user_tier ON ai_quotas(user_tier) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_ai_quotas_daily_reset ON ai_quotas(daily_reset_at) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_ai_quotas_monthly_reset ON ai_quotas(monthly_reset_at) WHERE deleted_at IS NULL;

-- GIN indexes for JSONB columns
CREATE INDEX IF NOT EXISTS idx_ai_usage_request_payload ON ai_usage USING GIN(request_payload) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_ai_usage_response_payload ON ai_usage USING GIN(response_payload) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_ai_models_capabilities ON ai_models USING GIN(capabilities) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_ai_models_configuration ON ai_models USING GIN(configuration) WHERE deleted_at IS NULL;

-- rollback DROP INDEX IF EXISTS idx_ai_usage_user_id;
-- rollback DROP INDEX IF EXISTS idx_ai_usage_created_at;
-- rollback DROP INDEX IF EXISTS idx_ai_usage_user_created;
-- rollback DROP INDEX IF EXISTS idx_ai_usage_session_id;
-- rollback DROP INDEX IF EXISTS idx_ai_usage_trace_id;
-- rollback DROP INDEX IF EXISTS idx_ai_usage_provider;
-- rollback DROP INDEX IF EXISTS idx_ai_usage_model_name;
-- rollback DROP INDEX IF EXISTS idx_ai_usage_feature;
-- rollback DROP INDEX IF EXISTS idx_ai_usage_success;
-- rollback DROP INDEX IF EXISTS idx_ai_models_provider;
-- rollback DROP INDEX IF EXISTS idx_ai_models_enabled;
-- rollback DROP INDEX IF EXISTS idx_ai_models_priority;
-- rollback DROP INDEX IF EXISTS idx_ai_budgets_user_id;
-- rollback DROP INDEX IF EXISTS idx_ai_budgets_daily_reset;
-- rollback DROP INDEX IF EXISTS idx_ai_budgets_monthly_reset;
-- rollback DROP INDEX IF EXISTS idx_ai_quotas_user_id;
-- rollback DROP INDEX IF EXISTS idx_ai_quotas_user_tier;
-- rollback DROP INDEX IF EXISTS idx_ai_quotas_daily_reset;
-- rollback DROP INDEX IF EXISTS idx_ai_quotas_monthly_reset;
-- rollback DROP INDEX IF EXISTS idx_ai_usage_request_payload;
-- rollback DROP INDEX IF EXISTS idx_ai_usage_response_payload;
-- rollback DROP INDEX IF EXISTS idx_ai_models_capabilities;
-- rollback DROP INDEX IF EXISTS idx_ai_models_configuration;
