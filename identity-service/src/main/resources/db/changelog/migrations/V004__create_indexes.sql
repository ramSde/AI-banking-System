-- ═══════════════════════════════════════════════════════════════════════
-- V004: Create indexes for performance optimization
-- Indexes on foreign keys, query-hot columns, and composite lookups
-- ═══════════════════════════════════════════════════════════════════════

-- ── USERS TABLE INDEXES ──────────────────────────────────────────────

-- Index on email (already unique, but explicit for query optimization)
CREATE INDEX idx_users_email ON users(email) WHERE deleted_at IS NULL;

-- Index on phone_number for lookup
CREATE INDEX idx_users_phone_number ON users(phone_number) WHERE deleted_at IS NULL AND phone_number IS NOT NULL;

-- Index on username for lookup
CREATE INDEX idx_users_username ON users(username) WHERE deleted_at IS NULL AND username IS NOT NULL;

-- Index on status for filtering active users
CREATE INDEX idx_users_status ON users(status) WHERE deleted_at IS NULL;

-- Index on locked_until for finding locked accounts
CREATE INDEX idx_users_locked_until ON users(locked_until) WHERE locked_until IS NOT NULL AND deleted_at IS NULL;

-- Composite index for authentication queries (email + status)
CREATE INDEX idx_users_email_status ON users(email, status) WHERE deleted_at IS NULL;

-- Index on created_at for sorting/filtering by registration date
CREATE INDEX idx_users_created_at ON users(created_at DESC) WHERE deleted_at IS NULL;

-- ── CREDENTIALS TABLE INDEXES ────────────────────────────────────────

-- Index on user_id (foreign key - already unique but explicit)
CREATE INDEX idx_credentials_user_id ON credentials(user_id) WHERE deleted_at IS NULL;

-- Index on password_changed_at for password expiry checks
CREATE INDEX idx_credentials_password_changed_at ON credentials(password_changed_at) WHERE deleted_at IS NULL;

-- ── REFRESH_TOKEN_AUDIT TABLE INDEXES ────────────────────────────────

-- Index on user_id (foreign key) for user token lookups
CREATE INDEX idx_refresh_token_user_id ON refresh_token_audit(user_id) WHERE deleted_at IS NULL;

-- Index on token_hash (already unique, but explicit for validation queries)
CREATE INDEX idx_refresh_token_hash ON refresh_token_audit(token_hash) WHERE deleted_at IS NULL;

-- Index on token_family_id for rotation chain queries
CREATE INDEX idx_refresh_token_family_id ON refresh_token_audit(token_family_id) WHERE deleted_at IS NULL;

-- Index on status for filtering active tokens
CREATE INDEX idx_refresh_token_status ON refresh_token_audit(status) WHERE deleted_at IS NULL;

-- Index on expires_at for cleanup jobs
CREATE INDEX idx_refresh_token_expires_at ON refresh_token_audit(expires_at) WHERE deleted_at IS NULL;

-- Composite index for token validation (user_id + status + expires_at)
CREATE INDEX idx_refresh_token_validation ON refresh_token_audit(user_id, status, expires_at) WHERE deleted_at IS NULL;

-- Index on device_id for device-based queries
CREATE INDEX idx_refresh_token_device_id ON refresh_token_audit(device_id) WHERE deleted_at IS NULL AND device_id IS NOT NULL;

-- Index on ip_address for security analysis
CREATE INDEX idx_refresh_token_ip_address ON refresh_token_audit(ip_address) WHERE deleted_at IS NULL AND ip_address IS NOT NULL;

-- Index on created_at for audit queries
CREATE INDEX idx_refresh_token_created_at ON refresh_token_audit(created_at DESC) WHERE deleted_at IS NULL;

-- Rollback
-- rollback DROP INDEX IF EXISTS idx_users_email;
-- rollback DROP INDEX IF EXISTS idx_users_phone_number;
-- rollback DROP INDEX IF EXISTS idx_users_username;
-- rollback DROP INDEX IF EXISTS idx_users_status;
-- rollback DROP INDEX IF EXISTS idx_users_locked_until;
-- rollback DROP INDEX IF EXISTS idx_users_email_status;
-- rollback DROP INDEX IF EXISTS idx_users_created_at;
-- rollback DROP INDEX IF EXISTS idx_credentials_user_id;
-- rollback DROP INDEX IF EXISTS idx_credentials_password_changed_at;
-- rollback DROP INDEX IF EXISTS idx_refresh_token_user_id;
-- rollback DROP INDEX IF EXISTS idx_refresh_token_hash;
-- rollback DROP INDEX IF EXISTS idx_refresh_token_family_id;
-- rollback DROP INDEX IF EXISTS idx_refresh_token_status;
-- rollback DROP INDEX IF EXISTS idx_refresh_token_expires_at;
-- rollback DROP INDEX IF EXISTS idx_refresh_token_validation;
-- rollback DROP INDEX IF EXISTS idx_refresh_token_device_id;
-- rollback DROP INDEX IF EXISTS idx_refresh_token_ip_address;
-- rollback DROP INDEX IF EXISTS idx_refresh_token_created_at;
