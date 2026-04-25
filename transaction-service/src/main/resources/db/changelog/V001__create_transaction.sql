--liquibase formatted sql

--changeset transaction-service:1
--comment: Create transaction table with all required fields

CREATE TABLE transaction (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reference_number VARCHAR(50) NOT NULL UNIQUE,
    idempotency_key VARCHAR(255) UNIQUE,
    transaction_type VARCHAR(20) NOT NULL,
    transaction_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    source_account_id UUID,
    destination_account_id UUID,
    amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    description TEXT,
    metadata JSONB,
    initiated_by UUID NOT NULL,
    initiated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    failed_at TIMESTAMPTZ,
    failure_reason TEXT,
    reversed_at TIMESTAMPTZ,
    reversal_reference VARCHAR(50),
    parent_transaction_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_transaction_type CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER', 'PAYMENT', 'REFUND', 'FEE', 'INTEREST', 'REVERSAL')),
    CONSTRAINT chk_transaction_status CHECK (transaction_status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REVERSED')),
    CONSTRAINT chk_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_transfer_accounts CHECK (
        (transaction_type = 'TRANSFER' AND source_account_id IS NOT NULL AND destination_account_id IS NOT NULL) OR
        (transaction_type != 'TRANSFER')
    )
);

COMMENT ON TABLE transaction IS 'Core transaction table storing all financial transactions';
COMMENT ON COLUMN transaction.id IS 'Primary key UUID';
COMMENT ON COLUMN transaction.reference_number IS 'Unique transaction reference number';
COMMENT ON COLUMN transaction.idempotency_key IS 'Idempotency key for duplicate prevention';
COMMENT ON COLUMN transaction.transaction_type IS 'Type: DEPOSIT, WITHDRAWAL, TRANSFER, PAYMENT, REFUND, FEE, INTEREST, REVERSAL';
COMMENT ON COLUMN transaction.transaction_status IS 'Status: PENDING, PROCESSING, COMPLETED, FAILED, REVERSED';
COMMENT ON COLUMN transaction.source_account_id IS 'Source account (debit)';
COMMENT ON COLUMN transaction.destination_account_id IS 'Destination account (credit)';
COMMENT ON COLUMN transaction.amount IS 'Transaction amount (scale=2, HALF_UP)';
COMMENT ON COLUMN transaction.currency IS 'ISO 4217 currency code';
COMMENT ON COLUMN transaction.description IS 'Human-readable description';
COMMENT ON COLUMN transaction.metadata IS 'Additional metadata as JSON';
COMMENT ON COLUMN transaction.initiated_by IS 'User who initiated the transaction';
COMMENT ON COLUMN transaction.parent_transaction_id IS 'Parent transaction for reversals';
COMMENT ON COLUMN transaction.version IS 'Optimistic locking version';

--rollback DROP TABLE transaction;
