-- liquibase formatted sql

-- changeset liquibase:4
-- comment: Create indexes for performance optimization

-- Translation Keys indexes
CREATE INDEX idx_translation_keys_category ON translation_keys(category) WHERE deleted_at IS NULL;
CREATE INDEX idx_translation_keys_key_name ON translation_keys(key_name) WHERE deleted_at IS NULL;
CREATE INDEX idx_translation_keys_is_dynamic ON translation_keys(is_dynamic) WHERE deleted_at IS NULL;

-- Translations indexes
CREATE INDEX idx_translations_key_id ON translations(translation_key_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_translations_locale ON translations(locale_code) WHERE deleted_at IS NULL;
CREATE INDEX idx_translations_key_locale ON translations(translation_key_id, locale_code) WHERE deleted_at IS NULL;
CREATE INDEX idx_translations_auto_translated ON translations(is_auto_translated) WHERE deleted_at IS NULL;

-- Supported Locales indexes
CREATE INDEX idx_supported_locales_active ON supported_locales(is_active) WHERE deleted_at IS NULL;
CREATE INDEX idx_supported_locales_order ON supported_locales(display_order) WHERE deleted_at IS NULL;

-- rollback DROP INDEX IF EXISTS idx_translation_keys_category;
-- rollback DROP INDEX IF EXISTS idx_translation_keys_key_name;
-- rollback DROP INDEX IF EXISTS idx_translation_keys_is_dynamic;
-- rollback DROP INDEX IF EXISTS idx_translations_key_id;
-- rollback DROP INDEX IF EXISTS idx_translations_locale;
-- rollback DROP INDEX IF EXISTS idx_translations_key_locale;
-- rollback DROP INDEX IF EXISTS idx_translations_auto_translated;
-- rollback DROP INDEX IF EXISTS idx_supported_locales_active;
-- rollback DROP INDEX IF EXISTS idx_supported_locales_order;
