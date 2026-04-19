-- Liquibase formatted SQL
-- changeset device-service:1

-- Create device table
CREATE TABLE device (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    fingerprint_hash VARCHAR(255) NOT NULL UNIQUE,
    device_type VARCHAR(50) NOT NULL,
    device_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    trust_score INTEGER NOT NULL DEFAULT 30,
    last_seen_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Device Information
    browser VARCHAR(100),
    browser_version VARCHAR(50),
    os VARCHAR(100),
    os_version VARCHAR(50),
    device_name VARCHAR(255),
    
    -- Network Information
    ip_address VARCHAR(45),
    geolocation JSONB,
    
    -- Hardware Information
    screen_resolution VARCHAR(50),
    timezone VARCHAR(100),
    language VARCHAR(50),
    user_agent TEXT,
    
    -- Audit Fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT chk_trust_score CHECK (trust_score >= 0 AND trust_score <= 100),
    CONSTRAINT chk_device_type CHECK (device_type IN ('DESKTOP', 'MOBILE', 'TABLET', 'UNKNOWN')),
    CONSTRAINT chk_device_status CHECK (device_status IN ('ACTIVE', 'BLOCKED', 'SUSPICIOUS', 'TRUSTED'))
);

-- Indexes
CREATE INDEX idx_device_user_id ON device(user_id);
CREATE INDEX idx_device_fingerprint_hash ON device(fingerprint_hash);
CREATE INDEX idx_device_status ON device(device_status);
CREATE INDEX idx_device_trust_score ON device(trust_score);
CREATE INDEX idx_device_last_seen_at ON device(last_seen_at);
CREATE INDEX idx_device_created_at ON device(created_at);
CREATE INDEX idx_device_deleted_at ON device(deleted_at) WHERE deleted_at IS NULL;

-- Composite Indexes
CREATE INDEX idx_device_user_status ON device(user_id, device_status);
CREATE INDEX idx_device_user_trust ON device(user_id, trust_score);

-- Comments
COMMENT ON TABLE device IS 'Stores registered devices with fingerprinting and trust scoring';
COMMENT ON COLUMN device.id IS 'Unique device identifier';
COMMENT ON COLUMN device.user_id IS 'User who owns this device';
COMMENT ON COLUMN device.fingerprint_hash IS 'SHA-256 hash of device fingerprint';
COMMENT ON COLUMN device.device_type IS 'Type of device: DESKTOP, MOBILE, TABLET, UNKNOWN';
COMMENT ON COLUMN device.device_status IS 'Status: ACTIVE, BLOCKED, SUSPICIOUS, TRUSTED';
COMMENT ON COLUMN device.trust_score IS 'Trust score from 0 (untrusted) to 100 (fully trusted)';
COMMENT ON COLUMN device.last_seen_at IS 'Last time this device was used';
COMMENT ON COLUMN device.geolocation IS 'JSON object with latitude, longitude, city, country';
COMMENT ON COLUMN device.version IS 'Optimistic locking version';

-- rollback DROP TABLE device;
