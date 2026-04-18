-- ============================================
-- MFA Enrollment Table
-- ============================================

CREATE TABLE IF NOT EXISTS mfa_enrollment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    mfa_method VARCHAR(20) NOT NULL CHECK (mfa_method IN ('TOTP', 'SMS', 'EMAIL')),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'DISABLED', 'SUSPENDED')),
    totp_secret VARCHAR(255),
    phone_number VARCHAR(20),
    email VARCHAR(255),
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    verified_at TIMESTAMPTZ,
    last_used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE mfa_enrollment IS 'Stores MFA enrollment information for users';
COMMENT ON COLUMN mfa_enrollment.id IS 'Primary key';
COMMENT ON COLUMN mfa_enrollment.user_id IS 'Reference to user in identity service';
COMMENT ON COLUMN mfa_enrollment.mfa_method IS 'MFA method: TOTP, SMS, or EMAIL';
COMMENT ON COLUMN mfa_enrollment.status IS 'Enrollment status: ACTIVE, DISABLED, SUSPENDED';
COMMENT ON COLUMN mfa_enrollment.totp_secret IS 'Base32-encoded TOTP secret (for TOTP method only)';
COMMENT ON COLUMN mfa_enrollment.phone_number IS 'Phone number for SMS OTP (for SMS method only)';
COMMENT ON COLUMN mfa_enrollment.email IS 'Email address for email OTP (for EMAIL method only)';
COMMENT ON COLUMN mfa_enrollment.verified IS 'Whether the MFA method has been verified';
COMMENT ON COLUMN mfa_enrollment.verified_at IS 'Timestamp when MFA was verified';
COMMENT ON COLUMN mfa_enrollment.last_used_at IS 'Timestamp of last successful MFA verification';
COMMENT ON COLUMN mfa_enrollment.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN mfa_enrollment.updated_at IS 'Record last update timestamp';
COMMENT ON COLUMN mfa_enrollment.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN mfa_enrollment.version IS 'Optimistic locking version';

-- Rollback
--rollback DROP TABLE IF EXISTS mfa_enrollment;
