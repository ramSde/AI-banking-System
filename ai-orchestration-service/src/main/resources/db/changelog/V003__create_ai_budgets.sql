-- liquibase formatted sql

-- changeset ai-orchestration:3
-- comment: Create ai_budgets table for user budget tracking

CREATE TABLE IF NOT EXISTS ai_budgets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    daily_budget_usd DECIMAL(10,2) NOT NULL DEFAULT 10.00,
    monthly_budget_usd DECIMAL(10,2) NOT NULL DEFAULT 300.00,
    daily_spent_usd DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    monthly_spent_usd DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    daily_reset_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    monthly_reset_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    alert_threshold DECIMAL(3,2) NOT NULL DEFAULT 0.80,
    alert_sent BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE ai_budgets IS 'User AI budget allocations and tracking';
COMMENT ON COLUMN ai_budgets.id IS 'Unique identifier for the budget';
COMMENT ON COLUMN ai_budgets.user_id IS 'User this budget belongs to';
COMMENT ON COLUMN ai_budgets.daily_budget_usd IS 'Daily budget limit in USD';
COMMENT ON COLUMN ai_budgets.monthly_budget_usd IS 'Monthly budget limit in USD';
COMMENT ON COLUMN ai_budgets.daily_spent_usd IS 'Amount spent today in USD';
COMMENT ON COLUMN ai_budgets.monthly_spent_usd IS 'Amount spent this month in USD';
COMMENT ON COLUMN ai_budgets.daily_reset_at IS 'When daily budget resets';
COMMENT ON COLUMN ai_budgets.monthly_reset_at IS 'When monthly budget resets';
COMMENT ON COLUMN ai_budgets.alert_threshold IS 'Threshold for budget alerts (0.0-1.0)';
COMMENT ON COLUMN ai_budgets.alert_sent IS 'Whether alert has been sent';
COMMENT ON COLUMN ai_budgets.created_at IS 'Timestamp when record was created';
COMMENT ON COLUMN ai_budgets.updated_at IS 'Timestamp when record was last updated';
COMMENT ON COLUMN ai_budgets.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN ai_budgets.version IS 'Optimistic locking version';

-- rollback DROP TABLE IF EXISTS ai_budgets;
