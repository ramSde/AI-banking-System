--liquibase formatted sql

--changeset audit-service:3
--comment: Create indexes for audit_events table to optimize query performance

-- Index on event_id for unique lookups
CREATE INDEX IF NOT EXISTS idx_audit_events_event_id ON audit_events(event_id);

-- Index on entity_type and entity_id for entity-specific queries
CREATE INDEX IF NOT EXISTS idx_audit_events_entity ON audit_events(entity_type, entity_id);

-- Index on actor_user_id for user activity queries
CREATE INDEX IF NOT EXISTS idx_audit_events_actor_user_id ON audit_events(actor_user_id) WHERE actor_user_id IS NOT NULL;

-- Index on occurred_at for time-based queries
CREATE INDEX IF NOT EXISTS idx_audit_events_occurred_at ON audit_events(occurred_at DESC);

-- Composite index for common query pattern: entity + time range
CREATE INDEX IF NOT EXISTS idx_audit_events_entity_time ON audit_events(entity_type, entity_id, occurred_at DESC);

-- Composite index for user activity over time
CREATE INDEX IF NOT EXISTS idx_audit_events_user_time ON audit_events(actor_user_id, occurred_at DESC) WHERE actor_user_id IS NOT NULL;

-- Index on event_type for filtering by event type
CREATE INDEX IF NOT EXISTS idx_audit_events_event_type ON audit_events(event_type);

-- Index on trace_id for distributed tracing queries
CREATE INDEX IF NOT EXISTS idx_audit_events_trace_id ON audit_events(trace_id) WHERE trace_id IS NOT NULL;

-- Index on correlation_id for tracking related events
CREATE INDEX IF NOT EXISTS idx_audit_events_correlation_id ON audit_events(correlation_id) WHERE correlation_id IS NOT NULL;

-- Index on service_name for service-specific queries
CREATE INDEX IF NOT EXISTS idx_audit_events_service_name ON audit_events(service_name);

-- Index on action for filtering by action type
CREATE INDEX IF NOT EXISTS idx_audit_events_action ON audit_events(action);

-- Index on status for filtering by status
CREATE INDEX IF NOT EXISTS idx_audit_events_status ON audit_events(status);

-- Composite index for service + event type + time
CREATE INDEX IF NOT EXISTS idx_audit_events_service_event_time ON audit_events(service_name, event_type, occurred_at DESC);

-- GIN index on before_state JSONB for JSON queries
CREATE INDEX IF NOT EXISTS idx_audit_events_before_state_gin ON audit_events USING GIN (before_state);

-- GIN index on after_state JSONB for JSON queries
CREATE INDEX IF NOT EXISTS idx_audit_events_after_state_gin ON audit_events USING GIN (after_state);

-- GIN index on changes JSONB for JSON queries
CREATE INDEX IF NOT EXISTS idx_audit_events_changes_gin ON audit_events USING GIN (changes);

-- GIN index on metadata JSONB for JSON queries
CREATE INDEX IF NOT EXISTS idx_audit_events_metadata_gin ON audit_events USING GIN (metadata);

-- Index on created_at for record insertion time queries
CREATE INDEX IF NOT EXISTS idx_audit_events_created_at ON audit_events(created_at DESC);

COMMENT ON INDEX idx_audit_events_event_id IS 'Index for unique event ID lookups';
COMMENT ON INDEX idx_audit_events_entity IS 'Index for entity-specific audit queries';
COMMENT ON INDEX idx_audit_events_actor_user_id IS 'Index for user activity queries';
COMMENT ON INDEX idx_audit_events_occurred_at IS 'Index for time-based queries';
COMMENT ON INDEX idx_audit_events_entity_time IS 'Composite index for entity + time range queries';
COMMENT ON INDEX idx_audit_events_user_time IS 'Composite index for user activity over time';
COMMENT ON INDEX idx_audit_events_event_type IS 'Index for filtering by event type';
COMMENT ON INDEX idx_audit_events_trace_id IS 'Index for distributed tracing queries';
COMMENT ON INDEX idx_audit_events_correlation_id IS 'Index for tracking related events';
COMMENT ON INDEX idx_audit_events_service_name IS 'Index for service-specific queries';
COMMENT ON INDEX idx_audit_events_action IS 'Index for filtering by action type';
COMMENT ON INDEX idx_audit_events_status IS 'Index for filtering by status';
COMMENT ON INDEX idx_audit_events_service_event_time IS 'Composite index for service + event type + time queries';
COMMENT ON INDEX idx_audit_events_before_state_gin IS 'GIN index for JSON queries on before_state';
COMMENT ON INDEX idx_audit_events_after_state_gin IS 'GIN index for JSON queries on after_state';
COMMENT ON INDEX idx_audit_events_changes_gin IS 'GIN index for JSON queries on changes';
COMMENT ON INDEX idx_audit_events_metadata_gin IS 'GIN index for JSON queries on metadata';
COMMENT ON INDEX idx_audit_events_created_at IS 'Index for record insertion time queries';

--rollback DROP INDEX IF EXISTS idx_audit_events_event_id;
--rollback DROP INDEX IF EXISTS idx_audit_events_entity;
--rollback DROP INDEX IF EXISTS idx_audit_events_actor_user_id;
--rollback DROP INDEX IF EXISTS idx_audit_events_occurred_at;
--rollback DROP INDEX IF EXISTS idx_audit_events_entity_time;
--rollback DROP INDEX IF EXISTS idx_audit_events_user_time;
--rollback DROP INDEX IF EXISTS idx_audit_events_event_type;
--rollback DROP INDEX IF EXISTS idx_audit_events_trace_id;
--rollback DROP INDEX IF EXISTS idx_audit_events_correlation_id;
--rollback DROP INDEX IF EXISTS idx_audit_events_service_name;
--rollback DROP INDEX IF EXISTS idx_audit_events_action;
--rollback DROP INDEX IF EXISTS idx_audit_events_status;
--rollback DROP INDEX IF EXISTS idx_audit_events_service_event_time;
--rollback DROP INDEX IF EXISTS idx_audit_events_before_state_gin;
--rollback DROP INDEX IF EXISTS idx_audit_events_after_state_gin;
--rollback DROP INDEX IF EXISTS idx_audit_events_changes_gin;
--rollback DROP INDEX IF EXISTS idx_audit_events_metadata_gin;
--rollback DROP INDEX IF EXISTS idx_audit_events_created_at;
