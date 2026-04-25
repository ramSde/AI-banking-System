--liquibase formatted sql

--changeset account-service:2
--comment: Create account balance history table for audit trail

CREATE TABLE account_balance_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL,
    previous_balance DECIMAL(19, 2) NOT NULL,
    new_balance DECIMAL(19, 2) NOT NULL,
    change_amount DECIMAL(19, 2) NOT NULL,
    change_type VARCHAR(20) NOT NULL,
    transaction_id UUID,
    reference_number VARCHAR(100),
    description TEXT,
    performed_by UUID,
    performed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_change_type CHECK (change_type IN ('CREDIT', 'DEBIT', 'HOLD', 'RELEASE', 'ADJUSTMENT', 'INTEREST'))
);

COMMENT ON TABLE account_balance_history IS 'Immutable audit trail of all balance changes';
COMMENT ON COLUMN account_balance_history.id IS 'Primary key UUID';
COMMENT ON COLUMN account_balance_history.account_id IS 'Reference to account';
COMMENT ON COLUMN account_balance_history.previous_balance IS 'Balance before change';
COMMENT ON COLUMN account_balance_history.new_balance IS 'Balance after change';
COMMENT ON COLUMN account_balance_history.change_amount IS 'Amount of change (positive or negative)';
COMMENT ON COLUMN account_balance_history.change_type IS 'Type of balance change';
COMMENT ON COLUMN account_balance_history.transaction_id IS 'Related transaction ID if applicable';
COMMENT ON COLUMN account_balance_history.reference_number IS 'External reference number';
COMMENT ON COLUMN account_balance_history.description IS 'Human-readable description';
COMMENT ON COLUMN account_balance_history.performed_by IS 'User or system that performed the change';
COMMENT ON COLUMN account_balance_history.performed_at IS 'When the change occurred';

--rollback DROP TABLE account_balance_history;
