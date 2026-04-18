-- ============================================
-- Backup Codes Table
-- ============================================

CREATE TABLE IF NOT EXISTS backup_code (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    code_hash VARCHAR(255) NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE backup_code IS 'Stores bcrypt-hashed backup codes for account recovery';
COMMENT ON COLUMN backup_code.id IS 'Primary key';
COMMENT ON COLUMN backup_code.user_id IS 'Reference to user in identity service';
COMMENT ON COLUMN backup_code.code_hash IS 'BCrypt hash of the backup code';
COMMENT ON COLUMN backup_code.used IS 'Whether this backup code has been used';
COMMENT ON COLUMN backup_code.used_at IS 'Timestamp when backup code was used';
COMMENT ON COLUMN backup_code.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN backup_code.updated_at IS 'Record last update timestamp';
COMMENT ON COLUMN backup_code.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN backup_code.version IS 'Optimistic locking version';

-- Rollback
--rollback DROP TABLE IF EXISTS backup_code;
