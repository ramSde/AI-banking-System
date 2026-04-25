--liquibase formatted sql

--changeset user-service:1
--comment: Create user table with encrypted PII fields

CREATE TABLE IF NOT EXISTS "user" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(20) NOT NULL,
    phone_number_encrypted TEXT NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    first_name_encrypted TEXT NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    last_name_encrypted TEXT NOT NULL,
    date_of_birth DATE NOT NULL,
    date_of_birth_encrypted TEXT NOT NULL,
    address TEXT,
    address_encrypted TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100) NOT NULL DEFAULT 'IN',
    postal_code VARCHAR(20),
    user_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    kyc_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    kyc_verified_at TIMESTAMPTZ,
    last_login_at TIMESTAMPTZ,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    account_locked_until TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_user_status CHECK (user_status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'LOCKED', 'PENDING_VERIFICATION')),
    CONSTRAINT chk_kyc_status CHECK (kyc_status IN ('PENDING', 'IN_PROGRESS', 'VERIFIED', 'REJECTED', 'EXPIRED'))
);

COMMENT ON TABLE "user" IS 'User profiles with encrypted PII fields for regulatory compliance';
COMMENT ON COLUMN "user".id IS 'Unique user identifier';
COMMENT ON COLUMN "user".email IS 'User email address (unique, used for login)';
COMMENT ON COLUMN "user".phone_number IS 'Masked phone number for display';
COMMENT ON COLUMN "user".phone_number_encrypted IS 'Encrypted phone number (AES-256-GCM)';
COMMENT ON COLUMN "user".first_name IS 'Masked first name for display';
COMMENT ON COLUMN "user".first_name_encrypted IS 'Encrypted first name (AES-256-GCM)';
COMMENT ON COLUMN "user".last_name IS 'Masked last name for display';
COMMENT ON COLUMN "user".last_name_encrypted IS 'Encrypted last name (AES-256-GCM)';
COMMENT ON COLUMN "user".date_of_birth IS 'Masked date of birth for display';
COMMENT ON COLUMN "user".date_of_birth_encrypted IS 'Encrypted date of birth (AES-256-GCM)';
COMMENT ON COLUMN "user".address IS 'Masked address for display';
COMMENT ON COLUMN "user".address_encrypted IS 'Encrypted full address (AES-256-GCM)';
COMMENT ON COLUMN "user".user_status IS 'User account status';
COMMENT ON COLUMN "user".kyc_status IS 'KYC verification status';
COMMENT ON COLUMN "user".kyc_verified_at IS 'Timestamp when KYC was verified';
COMMENT ON COLUMN "user".last_login_at IS 'Last successful login timestamp';
COMMENT ON COLUMN "user".failed_login_attempts IS 'Count of consecutive failed login attempts';
COMMENT ON COLUMN "user".account_locked_until IS 'Account lock expiry timestamp';
COMMENT ON COLUMN "user".version IS 'Optimistic locking version';

-- Indexes
CREATE INDEX idx_user_email ON "user"(email) WHERE deleted_at IS NULL;
CREATE INDEX idx_user_status ON "user"(user_status) WHERE deleted_at IS NULL;
CREATE INDEX idx_user_kyc_status ON "user"(kyc_status) WHERE deleted_at IS NULL;
CREATE INDEX idx_user_country ON "user"(country) WHERE deleted_at IS NULL;
CREATE INDEX idx_user_created_at ON "user"(created_at);
CREATE INDEX idx_user_last_login ON "user"(last_login_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_user_deleted_at ON "user"(deleted_at) WHERE deleted_at IS NOT NULL;

--rollback DROP TABLE IF EXISTS "user";
