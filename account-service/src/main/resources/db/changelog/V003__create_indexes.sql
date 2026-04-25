--liquibase formatted sql

--changeset account-service:3
--comment: Create indexes for performance optimization

-- Account table indexes
CREATE INDEX idx_account_user_id ON account(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_account_account_number ON account(account_number) WHERE deleted_at IS NULL;
CREATE INDEX idx_account_iban ON account(iban) WHERE deleted_at IS NULL AND iban IS NOT NULL;
CREATE INDEX idx_account_status ON account(account_status) WHERE deleted_at IS NULL;
CREATE INDEX idx_account_type ON account(account_type) WHERE deleted_at IS NULL;
CREATE INDEX idx_account_user_status ON account(user_id, account_status) WHERE deleted_at IS NULL;
CREATE INDEX idx_account_created_at ON account(created_at DESC) WHERE deleted_at IS NULL;

-- Account balance history indexes
CREATE INDEX idx_balance_history_account_id ON account_balance_history(account_id);
CREATE INDEX idx_balance_history_transaction_id ON account_balance_history(transaction_id) WHERE transaction_id IS NOT NULL;
CREATE INDEX idx_balance_history_performed_at ON account_balance_history(performed_at DESC);
CREATE INDEX idx_balance_history_account_performed ON account_balance_history(account_id, performed_at DESC);

--rollback DROP INDEX IF EXISTS idx_account_user_id;
--rollback DROP INDEX IF EXISTS idx_account_account_number;
--rollback DROP INDEX IF EXISTS idx_account_iban;
--rollback DROP INDEX IF EXISTS idx_account_status;
--rollback DROP INDEX IF EXISTS idx_account_type;
--rollback DROP INDEX IF EXISTS idx_account_user_status;
--rollback DROP INDEX IF EXISTS idx_account_created_at;
--rollback DROP INDEX IF EXISTS idx_balance_history_account_id;
--rollback DROP INDEX IF EXISTS idx_balance_history_transaction_id;
--rollback DROP INDEX IF EXISTS idx_balance_history_performed_at;
--rollback DROP INDEX IF EXISTS idx_balance_history_account_performed;
