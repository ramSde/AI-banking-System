-- liquibase formatted sql

-- changeset liquibase:2
-- comment: Create translations table for storing translated messages

CREATE TABLE translations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    translation_key_id UUID NOT NULL,
    locale_code VARCHAR(10) NOT NULL,
    translated_value TEXT NOT NULL,
    is_auto_translated BOOLEAN NOT NULL DEFAULT FALSE,
    translation_source VARCHAR(50),
    quality_score DECIMAL(3,2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_translation_key FOREIGN KEY (translation_key_id) REFERENCES translation_keys(id) ON DELETE CASCADE,
    CONSTRAINT uq_key_locale UNIQUE (translation_key_id, locale_code)
);

COMMENT ON TABLE translations IS 'Stores translated messages for different locales';
COMMENT ON COLUMN translations.id IS 'Unique identifier for the translation';
COMMENT ON COLUMN translations.translation_key_id IS 'Reference to the translation key';
COMMENT ON COLUMN translations.locale_code IS 'Locale code (e.g., en, es, fr, de, hi, ar, zh)';
COMMENT ON COLUMN translations.translated_value IS 'The translated message';
COMMENT ON COLUMN translations.is_auto_translated IS 'Whether this was automatically translated via API';
COMMENT ON COLUMN translations.translation_source IS 'Source of translation (manual, google, deepl, etc.)';
COMMENT ON COLUMN translations.quality_score IS 'Quality score of translation (0.00 to 1.00)';
COMMENT ON COLUMN translations.created_at IS 'Timestamp when the translation was created';
COMMENT ON COLUMN translations.updated_at IS 'Timestamp when the translation was last updated';
COMMENT ON COLUMN translations.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN translations.version IS 'Version number for optimistic locking';

-- rollback DROP TABLE translations;
