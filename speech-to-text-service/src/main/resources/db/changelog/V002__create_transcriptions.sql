-- Liquibase formatted SQL
-- changeset banking-platform:2

-- =====================================================
-- Table: transcriptions
-- Description: Stores transcription results from Whisper API
-- =====================================================

CREATE TABLE transcriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    audio_file_id UUID NOT NULL REFERENCES audio_files(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    language_detected VARCHAR(10),
    confidence_score DECIMAL(5, 2),
    full_text TEXT,
    word_count INTEGER,
    processing_time_ms BIGINT,
    model_used VARCHAR(50),
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),
    CONSTRAINT chk_confidence CHECK (confidence_score IS NULL OR (confidence_score >= 0 AND confidence_score <= 100)),
    CONSTRAINT chk_word_count CHECK (word_count IS NULL OR word_count >= 0),
    CONSTRAINT chk_processing_time CHECK (processing_time_ms IS NULL OR processing_time_ms >= 0)
);

-- Comments
COMMENT ON TABLE transcriptions IS 'Stores transcription results from Whisper API';
COMMENT ON COLUMN transcriptions.id IS 'Unique identifier for the transcription';
COMMENT ON COLUMN transcriptions.audio_file_id IS 'Reference to the audio file';
COMMENT ON COLUMN transcriptions.user_id IS 'User who owns this transcription';
COMMENT ON COLUMN transcriptions.status IS 'Processing status: PENDING, PROCESSING, COMPLETED, FAILED';
COMMENT ON COLUMN transcriptions.language_detected IS 'Detected language code (ISO 639-1)';
COMMENT ON COLUMN transcriptions.confidence_score IS 'Overall confidence score (0-100)';
COMMENT ON COLUMN transcriptions.full_text IS 'Complete transcribed text';
COMMENT ON COLUMN transcriptions.word_count IS 'Total word count';
COMMENT ON COLUMN transcriptions.processing_time_ms IS 'Processing time in milliseconds';
COMMENT ON COLUMN transcriptions.model_used IS 'Model used for transcription (e.g., whisper-1)';
COMMENT ON COLUMN transcriptions.error_message IS 'Error message if transcription failed';
COMMENT ON COLUMN transcriptions.created_at IS 'Creation timestamp';
COMMENT ON COLUMN transcriptions.updated_at IS 'Last update timestamp';
COMMENT ON COLUMN transcriptions.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN transcriptions.version IS 'Version for optimistic locking';

-- rollback DROP TABLE transcriptions;
