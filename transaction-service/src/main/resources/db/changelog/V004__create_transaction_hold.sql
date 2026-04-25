--liquibase formatted sql

--changeset transaction-service:4
--comment: Create transaction_hold table for authorization holds

CREATE TABLE transaction_hold (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL,
    hold_reference VARCHAR(50) NOT NULL UNIQUE,
    amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    hold_type VARCHAR(20) NOT NULL,
    description TEXT,
    initiated_by UUID NOT NULL,
    initiated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL,
    released_at TIMESTAMPTZ,
    captured_transaction_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_hold_type CHECK (hold_type IN ('AUTHORIZATION', 'RESERVATION', 'PENDING')),
    CONSTRAINT chk_hold_amount_positive CHECK (amount > 0)
);

COMMENT ON TABLE transaction_hold IS 'Authorization holds on account balances';
COMMENT ON COLUMN transaction_hold.id IS 'Primary key UUID';
COMMENT ON COLUMN transaction_hold.account_id IS 'Account with hold';
COMMENT ON COLUMN transaction_hold.hold_reference IS 'Unique hold reference';
COMMENT ON COLUMN transaction_hold.amount IS 'Hold amount (scale=2, HALF_UP)';
COMMENT ON COLUMN transaction_hold.hold_type IS 'Type of hold';
COMMENT ON COLUMN transaction_hold.expires_at IS 'Hold expiration timestamp';
COMMENT ON COLUMN transaction_hold.released_at IS 'When hold was released';
COMMENT ON COLUMN transaction_hold.captured_transaction_id IS 'Transaction that captured this hold';

--rollback DROP TABLE transaction_hold;
