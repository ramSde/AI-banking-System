-- Liquibase formatted SQL
-- changeset device-service:4

-- This migration seeds reference data for device types and statuses
-- No actual data insertion needed as enums are handled at application level
-- This file exists to maintain migration sequence consistency

-- Create indexes for performance optimization on reference data queries
CREATE INDEX IF NOT EXISTS idx_device_type_status ON device(device_type, device_status);
CREATE INDEX IF NOT EXISTS idx_device_trust_range ON device(trust_score) WHERE trust_score BETWEEN 0 AND 30;
CREATE INDEX IF NOT EXISTS idx_device_trusted_range ON device(trust_score) WHERE trust_score BETWEEN 70 AND 100;

-- Create partial indexes for common queries
CREATE INDEX IF NOT EXISTS idx_device_active_trusted ON device(user_id, trust_score) 
    WHERE device_status = 'ACTIVE' AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_device_recent_activity ON device(last_seen_at) 
    WHERE last_seen_at >= NOW() - INTERVAL '30 days' AND deleted_at IS NULL;

-- Add comments for reference data understanding
COMMENT ON COLUMN device.device_type IS 'Device types: DESKTOP (computers), MOBILE (phones), TABLET (tablets), UNKNOWN (unidentified)';
COMMENT ON COLUMN device.device_status IS 'Device statuses: ACTIVE (normal use), BLOCKED (security block), SUSPICIOUS (under review), TRUSTED (verified safe)';

-- Create function for trust score categorization (used in queries)
CREATE OR REPLACE FUNCTION get_trust_category(score INTEGER)
RETURNS VARCHAR(20) AS $$
BEGIN
    CASE 
        WHEN score BETWEEN 0 AND 30 THEN RETURN 'LOW';
        WHEN score BETWEEN 31 AND 60 THEN RETURN 'MEDIUM';
        WHEN score BETWEEN 61 AND 85 THEN RETURN 'HIGH';
        WHEN score BETWEEN 86 AND 100 THEN RETURN 'VERY_HIGH';
        ELSE RETURN 'UNKNOWN';
    END CASE;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

COMMENT ON FUNCTION get_trust_category(INTEGER) IS 'Categorizes trust scores into LOW/MEDIUM/HIGH/VERY_HIGH ranges';

-- rollback DROP FUNCTION IF EXISTS get_trust_category(INTEGER); DROP INDEX IF EXISTS idx_device_type_status; DROP INDEX IF EXISTS idx_device_trust_range; DROP INDEX IF EXISTS idx_device_trusted_range; DROP INDEX IF EXISTS idx_device_active_trusted; DROP INDEX IF EXISTS idx_device_recent_activity;