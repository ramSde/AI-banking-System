-- ═══════════════════════════════════════════════════════════════════════
-- V002: Create credentials table
-- Stores user authentication credentials (bcrypt hashed passwords)
-- ═══════════════════════════════════════════════════════════════════════

CREATE TABLE credentials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Foreign key to users
    user_id UUID NOT NULL,
    
    -- Credential fields
    password_hash VARCHAR(255) NOT NULL,
    password_changed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    must_change_password BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- Password history (for preventing reuse)
    previous_password_hashes TEXT[],
    
    -- Audit fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    
    -- Foreign key constraint
    CONSTRAINT fk_credentials_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Unique constraint: one credential record per user
    CONSTRAINT uk_credentials_user_id UNIQUE (user_id)
);

-- Comments
COMMENT ON TABLE credentials IS 'User authentication credentials (bcrypt hashed passwords)';
COMMENT ON COLUMN credentials.id IS 'Unique credential record identifier';
COMMENT ON COLUMN credentials.user_id IS 'Foreign key to users table';
COMMENT ON COLUMN credentials.password_hash IS 'BCrypt hashed password (cost factor 12)';
COMMENT ON COLUMN credentials.password_changed_at IS 'Timestamp of last password change';
COMMENT ON COLUMN credentials.must_change_password IS 'Flag indicating user must change password on next login';
COMMENT ON COLUMN credentials.previous_password_hashes IS 'Array of previous password hashes (for preventing reuse)';
COMMENT ON COLUMN credentials.created_at IS 'Record creation timestamp (UTC)';
COMMENT ON COLUMN credentials.updated_at IS 'Record last update timestamp (UTC)';
COMMENT ON COLUMN credentials.deleted_at IS 'Soft delete timestamp (NULL if not deleted)';
COMMENT ON COLUMN credentials.version IS 'Optimistic locking version number';

-- Rollback
-- rollback DROP TABLE IF EXISTS credentials CASCADE;
