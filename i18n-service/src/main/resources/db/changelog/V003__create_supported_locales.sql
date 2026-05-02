-- liquibase formatted sql

-- changeset liquibase:3
-- comment: Create supported_locales table for managing supported languages

CREATE TABLE supported_locales (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    locale_code VARCHAR(10) NOT NULL UNIQUE,
    language_name VARCHAR(100) NOT NULL,
    native_name VARCHAR(100) NOT NULL,
    is_rtl BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE supported_locales IS 'Stores supported locales/languages configuration';
COMMENT ON COLUMN supported_locales.locale_code IS 'Locale code (e.g., en, es, fr, de, hi, ar, zh)';
COMMENT ON COLUMN supported_locales.language_name IS 'English name of the language';
COMMENT ON COLUMN supported_locales.native_name IS 'Native name of the language';
COMMENT ON COLUMN supported_locales.is_rtl IS 'Whether this is a right-to-left language';
COMMENT ON COLUMN supported_locales.is_active IS 'Whether this locale is currently active';
COMMENT ON COLUMN supported_locales.display_order IS 'Display order in UI';

-- rollback DROP TABLE supported_locales;
