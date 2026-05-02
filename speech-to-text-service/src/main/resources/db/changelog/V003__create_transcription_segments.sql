-- Liquibase formatted SQL
-- changeset banking-platform:3

-- =====================================================
-- Table: transcription_segments
-- Description: Stores individual segments of transcription with timestamps
-- =====================================================

CREATE TABLE transcription_segments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transcription_id UUID NOT NULL REFERENCES transcriptions(id) ON DELETE CASCADE,
    segment_index INTEGER NOT NULL,
    start_time_seconds DECIMAL(10, 3) NOT NULL,
    end_time_seconds DECIMAL(10, 3) NOT NULL,
    text TEXT NOT NULL,
    speaker_id VARCHAR(50),
    confidence_score DECIMAL(5, 2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    CONSTRAINT chk_time_order CHECK (end_time_seconds > start_time_seconds),
    CONSTRAINT chk_start_time CHECK (start_time_seconds >= 0),
    CONSTRAINT chk_segment_confidence CHECK (confidence_score IS NULL OR (confidence_score >= 0 AND confidence_score <= 100)),
    CONSTRAINT uq_transcription_segment UNIQUE (transcription_id, segment_index)
);

-- Comments
COMMENT ON TABLE transcription_segments IS 'Stores individual segments of transcription with timestamps';
COMMENT ON COLUMN transcription_segments.id IS 'Unique identifier for the segment';
COMMENT ON COLUMN transcription_segments.transcription_id IS 'Reference to the parent transcription';
COMMENT ON COLUMN transcription_segments.segment_index IS 'Segment index (order in the transcription)';
COMMENT ON COLUMN transcription_segments.start_time_seconds IS 'Start time in seconds';
COMMENT ON COLUMN transcription_segments.end_time_seconds IS 'End time in seconds';
COMMENT ON COLUMN transcription_segments.text IS 'Transcribed text for this segment';
COMMENT ON COLUMN transcription_segments.speaker_id IS 'Speaker identifier (for diarization)';
COMMENT ON COLUMN transcription_segments.confidence_score IS 'Confidence score for this segment (0-100)';
COMMENT ON COLUMN transcription_segments.created_at IS 'Creation timestamp';

-- rollback DROP TABLE transcription_segments;
