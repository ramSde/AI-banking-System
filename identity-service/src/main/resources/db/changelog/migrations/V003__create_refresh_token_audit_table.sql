-- ═══════════════════════════════════════════════════════════════════════
-- V003: Create refresh_token_audit table
-- Stores bcrypt hashes of refresh tokens for validation and rotation tracking
-- ═══════════════════════════════════════════════════════════════════════

CREATE TABLE refresh_token_audit (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Foreign key to users
    user_id UUID NOT NULL,
    
    -- Token fields
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    token_family_id UUID NOT NULL,
    
    -- Metadata
    issued_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    replaced_by_token_id UUID,
    
    -- Device/session tracking
    device_id VARCHAR(255),
    ip_address VARCHAR(45),
    user_agent TEXT,
    
    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    
    -- Audit fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    
    -- Foreign key constraint
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Self-referential foreign key for token rotation chain
    CONSTRAINT fk_refresh_token_replaced_by FOREIGN KEY (replaced_by_token_id) REFERENCES refresh_token_audit(id) ON DELETE SET NULL,
    
    -- Constraints
    CONSTRAINT chk_refresh_token_status CHECK (status IN ('ACTIVE', 'REVOKED', 'EXPIRED', 'REPLACED'))
);

-- Comments
COMMENT ON TABLE refresh_token_audit IS 'Audit trail for refresh tokens with bcrypt hashes for validation';
COMMENT ON COLUMN refresh_token_audit.id IS 'Unique refresh token audit record identifier';
COMMENT ON COLUMN refresh_token_audit.user_id IS 'Foreign key to users table';
COMMENT ON COLUMN refresh_token_audit.token_hash IS 'BCrypt hash of the refresh token (for validation)';
COMMENT ON COLUMN refresh_token_audit.token_family_id IS 'Token family identifier for rotation chain tracking';
COMMENT ON COLUMN refresh_token_audit.issued_at IS 'Timestamp when token was issued';
COMMENT ON COLUMN refresh_token_audit.expires_at IS 'Timestamp when token expires';
COMMENT ON COLUMN refresh_token_audit.revoked_at IS 'Timestamp when token was revoked (NULL if not revoked)';
COMMENT ON COLUMN refresh_token_audit.replaced_by_token_id IS 'ID of the token that replaced this one (rotation chain)';
COMMENT ON COLUMN refresh_token_audit.device_id IS 'Device identifier that requested the token';
COMMENT ON COLUMN refresh_token_audit.ip_address IS 'IP address that requested the token';
COMMENT ON COLUMN refresh_token_audit.user_agent IS 'User agent string of the requesting client';
COMMENT ON COLUMN refresh_token_audit.status IS 'Token status: ACTIVE, REVOKED, EXPIRED, REPLACED';
COMMENT ON COLUMN refresh_token_audit.created_at IS 'Record creation timestamp (UTC)';
COMMENT ON COLUMN refresh_token_audit.updated_at IS 'Record last update timestamp (UTC)';
COMMENT ON COLUMN refresh_token_audit.deleted_at IS 'Soft delete timestamp (NULL if not deleted)';
COMMENT ON COLUMN refresh_token_audit.version IS 'Optimistic locking version number';

-- Rollback
-- rollback DROP TABLE IF EXISTS refresh_token_audit CASCADE;
