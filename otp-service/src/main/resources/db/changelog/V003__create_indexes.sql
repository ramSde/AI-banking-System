-- ============================================
-- Indexes for Performance Optimization
-- ============================================

-- MFA Enrollment Indexes
CREATE INDEX IF NOT EXISTS idx_mfa_enrollment_user_id ON mfa_enrollment(user_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_mfa_enrollment_user_method ON mfa_enrollment(user_id, mfa_method) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_mfa_enrollment_status ON mfa_enrollment(status) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_mfa_enrollment_verified ON mfa_enrollment(verified) WHERE deleted_at IS NULL;

-- Backup Code Indexes
CREATE INDEX IF NOT EXISTS idx_backup_code_user_id ON backup_code(user_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_backup_code_user_unused ON backup_code(user_id, used) WHERE deleted_at IS NULL AND used = FALSE;

-- Unique Constraints
CREATE UNIQUE INDEX IF NOT EXISTS idx_mfa_enrollment_user_method_unique 
    ON mfa_enrollment(user_id, mfa_method) 
    WHERE deleted_at IS NULL AND status = 'ACTIVE';

-- Rollback
--rollback DROP INDEX IF EXISTS idx_mfa_enrollment_user_id;
--rollback DROP INDEX IF EXISTS idx_mfa_enrollment_user_method;
--rollback DROP INDEX IF EXISTS idx_mfa_enrollment_status;
--rollback DROP INDEX IF EXISTS idx_mfa_enrollment_verified;
--rollback DROP INDEX IF EXISTS idx_backup_code_user_id;
--rollback DROP INDEX IF EXISTS idx_backup_code_user_unused;
--rollback DROP INDEX IF EXISTS idx_mfa_enrollment_user_method_unique;
