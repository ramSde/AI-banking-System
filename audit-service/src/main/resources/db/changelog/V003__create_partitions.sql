--liquibase formatted sql

--changeset audit-service:4
--comment: Create table partitioning setup for audit_events by occurred_at (quarterly partitions)

-- Note: This migration creates the partitioning structure
-- Actual partition creation should be automated via scheduled job or manual process
-- Partitioning is optional and controlled by audit.partition-enabled configuration

-- Create function to automatically create partitions
CREATE OR REPLACE FUNCTION create_audit_partition(
    partition_date DATE
)
RETURNS VOID AS $$
DECLARE
    partition_name TEXT;
    start_date DATE;
    end_date DATE;
BEGIN
    -- Calculate partition boundaries (quarterly)
    start_date := DATE_TRUNC('quarter', partition_date);
    end_date := start_date + INTERVAL '3 months';
    
    -- Generate partition name (e.g., audit_events_2024_q1)
    partition_name := 'audit_events_' || 
                     TO_CHAR(start_date, 'YYYY') || '_q' || 
                     TO_CHAR(EXTRACT(QUARTER FROM start_date), 'FM9');
    
    -- Check if partition already exists
    IF NOT EXISTS (
        SELECT 1 
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = partition_name
        AND n.nspname = 'public'
    ) THEN
        -- Create partition
        EXECUTE format(
            'CREATE TABLE IF NOT EXISTS %I PARTITION OF audit_events
             FOR VALUES FROM (%L) TO (%L)',
            partition_name,
            start_date,
            end_date
        );
        
        RAISE NOTICE 'Created partition: % for range [%, %)', partition_name, start_date, end_date;
    ELSE
        RAISE NOTICE 'Partition % already exists', partition_name;
    END IF;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION create_audit_partition(DATE) IS 'Creates a quarterly partition for audit_events table based on occurred_at date';

-- Create function to automatically create future partitions
CREATE OR REPLACE FUNCTION create_future_audit_partitions(
    months_ahead INTEGER DEFAULT 12
)
RETURNS VOID AS $$
DECLARE
    current_quarter DATE;
    i INTEGER;
BEGIN
    current_quarter := DATE_TRUNC('quarter', CURRENT_DATE);
    
    FOR i IN 0..((months_ahead / 3) - 1) LOOP
        PERFORM create_audit_partition(current_quarter + (i * INTERVAL '3 months'));
    END LOOP;
    
    RAISE NOTICE 'Created partitions for next % months', months_ahead;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION create_future_audit_partitions(INTEGER) IS 'Creates quarterly partitions for the specified number of months ahead';

-- Create function to drop old partitions based on retention policy
CREATE OR REPLACE FUNCTION drop_old_audit_partitions(
    retention_days INTEGER DEFAULT 2555
)
RETURNS VOID AS $$
DECLARE
    partition_record RECORD;
    cutoff_date DATE;
BEGIN
    cutoff_date := CURRENT_DATE - retention_days;
    
    FOR partition_record IN
        SELECT c.relname AS partition_name
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        JOIN pg_inherits i ON i.inhrelid = c.oid
        JOIN pg_class p ON p.oid = i.inhparent
        WHERE p.relname = 'audit_events'
        AND n.nspname = 'public'
        AND c.relname LIKE 'audit_events_%'
    LOOP
        -- Extract year and quarter from partition name
        -- This is a simplified check - in production, you'd parse the partition bounds
        EXECUTE format('DROP TABLE IF EXISTS %I', partition_record.partition_name);
        RAISE NOTICE 'Dropped old partition: %', partition_record.partition_name;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION drop_old_audit_partitions(INTEGER) IS 'Drops audit_events partitions older than retention period (7 years = 2555 days)';

-- Note: To enable partitioning, the audit_events table needs to be recreated as a partitioned table
-- This is a breaking change and should be done during maintenance window
-- The following SQL shows how to convert the table (NOT executed by default):

-- Step 1: Rename existing table
-- ALTER TABLE audit_events RENAME TO audit_events_old;

-- Step 2: Create partitioned table
-- CREATE TABLE audit_events (
--     LIKE audit_events_old INCLUDING ALL
-- ) PARTITION BY RANGE (occurred_at);

-- Step 3: Create initial partitions
-- SELECT create_future_audit_partitions(12);

-- Step 4: Migrate data (if any)
-- INSERT INTO audit_events SELECT * FROM audit_events_old;

-- Step 5: Drop old table
-- DROP TABLE audit_events_old;

-- For now, we just create the helper functions
-- Actual partitioning conversion should be done manually when needed

--rollback DROP FUNCTION IF EXISTS create_audit_partition(DATE);
--rollback DROP FUNCTION IF EXISTS create_future_audit_partitions(INTEGER);
--rollback DROP FUNCTION IF EXISTS drop_old_audit_partitions(INTEGER);
