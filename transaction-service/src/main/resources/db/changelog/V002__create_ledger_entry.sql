--liquibase formatted sql

--changeset transaction-service:2
--comment: Create ledger_entry table for double-entry bookkeeping

CREATE TABLE ledger_entry (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL,
    account_id UUID NOT NULL,
    entry_type VARCHAR(10) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    balance_before DECIMAL(19, 2) NOT NULL,
    balance_after DECIMAL(19, 2) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_entry_type CHECK (entry_type IN ('DEBIT', 'CREDIT')),
    CONSTRAINT chk_ledger_amount_positive CHECK (amount > 0)
);

COMMENT ON TABLE ledger_entry IS 'Double-entry ledger for all transactions';
COMMENT ON COLUMN ledger_entry.id IS 'Primary key UUID';
COMMENT ON COLUMN ledger_entry.transaction_id IS 'Reference to transaction';
COMMENT ON COLUMN ledger_entry.account_id IS 'Account affected by this entry';
COMMENT ON COLUMN ledger_entry.entry_type IS 'DEBIT or CREDIT';
COMMENT ON COLUMN ledger_entry.amount IS 'Entry amount (scale=2, HALF_UP)';
COMMENT ON COLUMN ledger_entry.balance_before IS 'Account balance before entry';
COMMENT ON COLUMN ledger_entry.balance_after IS 'Account balance after entry';

--rollback DROP TABLE ledger_entry;
