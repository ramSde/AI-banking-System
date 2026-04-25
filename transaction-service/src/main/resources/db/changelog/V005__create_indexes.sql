--liquibase formatted sql

--changeset transaction-service:5
--comment: Create indexes for performance optimization

-- Transaction table indexes
CREATE INDEX idx_transaction_reference ON transaction(reference_number) WHERE deleted_at IS NULL;
CREATE INDEX idx_transaction_idempotency_key ON transaction(idempotency_key) WHERE idempotency_key IS NOT NULL AND deleted_at IS NULL;
CREATE INDEX idx_transaction_source_account ON transaction(source_account_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_transaction_destination_account ON transaction(destination_account_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_transaction_initiated_by ON transaction(initiated_by) WHERE deleted_at IS NULL;
CREATE INDEX idx_transaction_status ON transaction(transaction_status) WHERE deleted_at IS NULL;
CREATE INDEX idx_transaction_type ON transaction(transaction_type) WHERE deleted_at IS NULL;
CREATE INDEX idx_transaction_initiated_at ON transaction(initiated_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_transaction_parent ON transaction(parent_transaction_id) WHERE parent_transaction_id IS NOT NULL;

-- Ledger entry indexes
CREATE INDEX idx_ledger_transaction_id ON ledger_entry(transaction_id);
CREATE INDEX idx_ledger_account_id ON ledger_entry(account_id);
CREATE INDEX idx_ledger_account_created ON ledger_entry(account_id, created_at DESC);
CREATE INDEX idx_ledger_entry_type ON ledger_entry(entry_type);

-- Idempotency keys indexes
CREATE INDEX idx_idempotency_expires_at ON idempotency_keys(expires_at);
CREATE INDEX idx_idempotency_transaction_id ON idempotency_keys(transaction_id);

-- Transaction hold indexes
CREATE INDEX idx_hold_account_id ON transaction_hold(account_id);
CREATE INDEX idx_hold_reference ON transaction_hold(hold_reference);
CREATE INDEX idx_hold_expires_at ON transaction_hold(expires_at) WHERE released_at IS NULL;
CREATE INDEX idx_hold_initiated_by ON transaction_hold(initiated_by);

--rollback DROP INDEX IF EXISTS idx_transaction_reference;
--rollback DROP INDEX IF EXISTS idx_transaction_idempotency_key;
--rollback DROP INDEX IF EXISTS idx_transaction_source_account;
--rollback DROP INDEX IF EXISTS idx_transaction_destination_account;
--rollback DROP INDEX IF EXISTS idx_transaction_initiated_by;
--rollback DROP INDEX IF EXISTS idx_transaction_status;
--rollback DROP INDEX IF EXISTS idx_transaction_type;
--rollback DROP INDEX IF EXISTS idx_transaction_initiated_at;
--rollback DROP INDEX IF EXISTS idx_transaction_parent;
--rollback DROP INDEX IF EXISTS idx_ledger_transaction_id;
--rollback DROP INDEX IF EXISTS idx_ledger_account_id;
--rollback DROP INDEX IF EXISTS idx_ledger_account_created;
--rollback DROP INDEX IF EXISTS idx_ledger_entry_type;
--rollback DROP INDEX IF EXISTS idx_idempotency_expires_at;
--rollback DROP INDEX IF EXISTS idx_idempotency_transaction_id;
--rollback DROP INDEX IF EXISTS idx_hold_account_id;
--rollback DROP INDEX IF EXISTS idx_hold_reference;
--rollback DROP INDEX IF EXISTS idx_hold_expires_at;
--rollback DROP INDEX IF EXISTS idx_hold_initiated_by;
