-- ═══════════════════════════════════════════════════════════════════════
-- V001: Create users table
-- Stores core user identity information
-- ═══════════════════════════════════════════════════════════════════════

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Identity fields
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    username VARCHAR(100) UNIQUE,
    
    -- Status fields
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- Security fields
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMPTZ,
    last_login_at TIMESTAMPTZ,
    last_login_ip VARCHAR(45),
    
    -- Audit fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    
    -- Constraints
    CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'LOCKED'))
);

-- Comments
COMMENT ON TABLE users IS 'Core user identity and authentication status';
COMMENT ON COLUMN users.id IS 'Unique user identifier (UUID)';
COMMENT ON COLUMN users.email IS 'User email address (unique, required)';
COMMENT ON COLUMN users.phone_number IS 'User phone number in E.164 format';
COMMENT ON COLUMN users.username IS 'Optional username (unique if provided)';
COMMENT ON COLUMN users.status IS 'User account status: ACTIVE, INACTIVE, SUSPENDED, LOCKED';
COMMENT ON COLUMN users.email_verified IS 'Whether email has been verified';
COMMENT ON COLUMN users.phone_verified IS 'Whether phone number has been verified';
COMMENT ON COLUMN users.failed_login_attempts IS 'Counter for consecutive failed login attempts';
COMMENT ON COLUMN users.locked_until IS 'Timestamp until which account is locked (NULL if not locked)';
COMMENT ON COLUMN users.last_login_at IS 'Timestamp of last successful login';
COMMENT ON COLUMN users.last_login_ip IS 'IP address of last successful login';
COMMENT ON COLUMN users.created_at IS 'Record creation timestamp (UTC)';
COMMENT ON COLUMN users.updated_at IS 'Record last update timestamp (UTC)';
COMMENT ON COLUMN users.deleted_at IS 'Soft delete timestamp (NULL if not deleted)';
COMMENT ON COLUMN users.version IS 'Optimistic locking version number';

-- Rollback
-- rollback DROP TABLE IF EXISTS users CASCADE;
