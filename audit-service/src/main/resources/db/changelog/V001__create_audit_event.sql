--liquibase formatted sql

--changeset audit-service:1
--comment: Create audit_events table for immutable audit trail

CREATE TABLE IF NOT EXISTS audit_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id VARCHAR(255) NOT NULL UNIQUE,
    event_type VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(255) NOT NULL,
    actor_user_id UUID,
    actor_username VARCHAR(255),
    actor_ip VARCHAR(45),
    actor_device_id VARCHAR(255),
    actor_user_agent TEXT,
    before_state JSONB,
    after_state JSONB,
    changes JSONB,
    occurred_at TIMESTAMPTZ NOT NULL,
    trace_id VARCHAR(255),
    span_id VARCHAR(255),
    correlation_id VARCHAR(255),
    session_id VARCHAR(255),
    service_name VARCHAR(100),
    action VARCHAR(100),
    status VARCHAR(50),
    error_message TEXT,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE audit_events IS 'Immutable audit trail for all banking operations - NO UPDATE or DELETE operations allowed';
COMMENT ON COLUMN audit_events.id IS 'Primary key UUID';
COMMENT ON COLUMN audit_events.event_id IS 'Unique event identifier from source service';
COMMENT ON COLUMN audit_events.event_type IS 'Type of audit event (e.g., ACCOUNT_CREATED, TRANSACTION_EXECUTED)';
COMMENT ON COLUMN audit_events.entity_type IS 'Type of entity being audited (e.g., ACCOUNT, TRANSACTION, USER)';
COMMENT ON COLUMN audit_events.entity_id IS 'ID of the entity being audited';
COMMENT ON COLUMN audit_events.actor_user_id IS 'UUID of the user who performed the action';
COMMENT ON COLUMN audit_events.actor_username IS 'Username of the actor';
COMMENT ON COLUMN audit_events.actor_ip IS 'IP address of the actor (IPv4 or IPv6)';
COMMENT ON COLUMN audit_events.actor_device_id IS 'Device fingerprint ID';
COMMENT ON COLUMN audit_events.actor_user_agent IS 'User agent string from the request';
COMMENT ON COLUMN audit_events.before_state IS 'State of the entity before the action (JSON)';
COMMENT ON COLUMN audit_events.after_state IS 'State of the entity after the action (JSON)';
COMMENT ON COLUMN audit_events.changes IS 'JSON diff between before and after states';
COMMENT ON COLUMN audit_events.occurred_at IS 'Timestamp when the event occurred (UTC)';
COMMENT ON COLUMN audit_events.trace_id IS 'Distributed tracing trace ID';
COMMENT ON COLUMN audit_events.span_id IS 'Distributed tracing span ID';
COMMENT ON COLUMN audit_events.correlation_id IS 'Correlation ID for tracking related events';
COMMENT ON COLUMN audit_events.session_id IS 'User session ID';
COMMENT ON COLUMN audit_events.service_name IS 'Name of the service that generated the event';
COMMENT ON COLUMN audit_events.action IS 'Action performed (CREATE, UPDATE, DELETE, etc.)';
COMMENT ON COLUMN audit_events.status IS 'Status of the action (SUCCESS, FAILURE, PENDING)';
COMMENT ON COLUMN audit_events.error_message IS 'Error message if action failed';
COMMENT ON COLUMN audit_events.metadata IS 'Additional metadata as JSON';
COMMENT ON COLUMN audit_events.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN audit_events.updated_at IS 'Record update timestamp (should never change for audit)';
COMMENT ON COLUMN audit_events.deleted_at IS 'Soft delete timestamp (should never be set for audit)';
COMMENT ON COLUMN audit_events.version IS 'Optimistic locking version';

--rollback DROP TABLE IF EXISTS audit_events;

--changeset audit-service:2
--comment: Create trigger to prevent updates and deletes on audit_events table

CREATE OR REPLACE FUNCTION prevent_audit_modification()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'UPDATE' THEN
        RAISE EXCEPTION 'UPDATE operations are not allowed on audit_events table';
    END IF;
    IF TG_OP = 'DELETE' THEN
        RAISE EXCEPTION 'DELETE operations are not allowed on audit_events table';
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER audit_events_immutable_trigger
BEFORE UPDATE OR DELETE ON audit_events
FOR EACH ROW
EXECUTE FUNCTION prevent_audit_modification();

COMMENT ON FUNCTION prevent_audit_modification() IS 'Prevents any UPDATE or DELETE operations on audit_events table to maintain immutability';

--rollback DROP TRIGGER IF EXISTS audit_events_immutable_trigger ON audit_events;
--rollback DROP FUNCTION IF EXISTS prevent_audit_modification();
