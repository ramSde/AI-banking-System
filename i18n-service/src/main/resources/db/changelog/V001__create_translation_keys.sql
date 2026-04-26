-- liquibase formatted sql

-- changeset liquibase:1
-- comment: Create translation_keys table for storing translation key definitions

CREATE TABLE translation_keys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key_name VARCHAR(500) NOT NULL UNIQUE,
    category VARCHAR(100) NOT NULL,
    description TEXT,
    default_value TEXT,
    is_dynamic BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE translation_keys IS 'Stores translation key definitions and metadata';
COMMENT ON COLUMN translation_keys.id IS 'Unique identifier for the translation key';
COMMENT ON COLUMN translation_keys.key_name IS 'Unique key name used for lookups (e.g., welcome.message)';
COMMENT ON COLUMN translation_keys.category IS 'Category for grouping keys (e.g., ui, email, sms, chat)';
COMMENT ON COLUMN translation_keys.description IS 'Description of what this key is used for';
COMMENT ON COLUMN translation_keys.default_value IS 'Default English value';
COMMENT ON COLUMN translation_keys.is_dynamic IS 'Whether this key requires dynamic translation via API';
COMMENT ON COLUMN translation_keys.created_at IS 'Timestamp when the key was created';
COMMENT ON COLUMN translation_keys.updated_at IS 'Timestamp when the key was last updated';
COMMENT ON COLUMN translation_keys.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN translation_keys.version IS 'Version number for optimistic locking';

-- rollback DROP TABLE translation_keys;
