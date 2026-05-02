-- Liquibase formatted SQL
-- changeset banking-platform:1

-- =====================================================
-- Table: audio_files
-- Description: Stores metadata for uploaded audio files
-- =====================================================

CREATE TABLE audio_files (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    filename VARCHAR(255) NOT NULL,
    original_format VARCHAR(10) NOT NULL,
    converted_format VARCHAR(10),
    file_size_bytes BIGINT NOT NULL,
    duration_seconds DECIMAL(10, 2),
    storage_path VARCHAR(500) NOT NULL,
    language_code VARCHAR(10),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT chk_file_size CHECK (file_size_bytes > 0),
    CONSTRAINT chk_duration CHECK (duration_seconds IS NULL OR duration_seconds > 0)
);

-- Comments
COMMENT ON TABLE audio_files IS 'Stores metadata for uploaded audio files';
COMMENT ON COLUMN audio_files.id IS 'Unique identifier for the audio file';
COMMENT ON COLUMN audio_files.user_id IS 'User who uploaded the audio file';
COMMENT ON COLUMN audio_files.filename IS 'Original filename';
COMMENT ON COLUMN audio_files.original_format IS 'Original audio format (MP3, WAV, etc.)';
COMMENT ON COLUMN audio_files.converted_format IS 'Format after conversion for Whisper API';
COMMENT ON COLUMN audio_files.file_size_bytes IS 'File size in bytes';
COMMENT ON COLUMN audio_files.duration_seconds IS 'Audio duration in seconds';
COMMENT ON COLUMN audio_files.storage_path IS 'Path in object storage (S3/MinIO)';
COMMENT ON COLUMN audio_files.language_code IS 'Language code (ISO 639-1)';
COMMENT ON COLUMN audio_files.created_at IS 'Creation timestamp';
COMMENT ON COLUMN audio_files.updated_at IS 'Last update timestamp';
COMMENT ON COLUMN audio_files.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN audio_files.version IS 'Version for optimistic locking';

-- rollback DROP TABLE audio_files;
