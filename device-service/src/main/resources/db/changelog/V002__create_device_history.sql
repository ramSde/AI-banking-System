-- Liquibase formatted SQL
-- changeset device-service:2

-- Create device_history table
CREATE TABLE device_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id UUID NOT NULL,
    user_id UUID NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    
    -- Event Details
    ip_address VARCHAR(45),
    geolocation JSONB,
    user_agent TEXT,
    
    -- Result
    success BOOLEAN NOT NULL DEFAULT true,
    failure_reason VARCHAR(255),
    
    -- Metadata
    metadata JSONB,
    
    -- Audit Fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    CONSTRAINT fk_device_history_device FOREIGN KEY (device_id) REFERENCES device(id) ON DELETE CASCADE,
    CONSTRAINT chk_event_type CHECK (event_type IN (
        'REGISTERED', 'LOGIN_SUCCESS', 'LOGIN_FAILED', 
        'TRUST_INCREASED', 'TRUST_DECREASED', 'BLOCKED', 
        'UNBLOCKED', 'ANOMALY_DETECTED', 'LOCATION_CHANGED'
    ))
);

-- Indexes
CREATE INDEX idx_device_history_device_id ON device_history(device_id);
CREATE INDEX idx_device_history_user_id ON device_history(user_id);
CREATE INDEX idx_device_history_event_type ON device_history(event_type);
CREATE INDEX idx_device_history_created_at ON device_history(created_at);
CREATE INDEX idx_device_history_success ON device_history(success);

-- Composite Indexes
CREATE INDEX idx_device_history_device_created ON device_history(device_id, created_at DESC);
CREATE INDEX idx_device_history_user_created ON device_history(user_id, created_at DESC);

-- Comments
COMMENT ON TABLE device_history IS 'Immutable audit log of all device events';
COMMENT ON COLUMN device_history.id IS 'Unique history entry identifier';
COMMENT ON COLUMN device_history.device_id IS 'Device that triggered this event';
COMMENT ON COLUMN device_history.user_id IS 'User associated with this event';
COMMENT ON COLUMN device_history.event_type IS 'Type of event that occurred';
COMMENT ON COLUMN device_history.geolocation IS 'JSON object with location data at time of event';
COMMENT ON COLUMN device_history.success IS 'Whether the event was successful';
COMMENT ON COLUMN device_history.metadata IS 'Additional event-specific data';

-- rollback DROP TABLE device_history;
