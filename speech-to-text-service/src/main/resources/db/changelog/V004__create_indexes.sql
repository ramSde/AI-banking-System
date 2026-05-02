-- Liquibase formatted SQL
-- changeset banking-platform:4

-- =====================================================
-- Indexes for Performance Optimization
-- =====================================================

-- Audio Files Indexes
CREATE INDEX idx_audio_files_user_id 
    ON audio_files(user_id) 
    WHERE deleted_at IS NULL;

CREATE INDEX idx_audio_files_created_at 
    ON audio_files(created_at DESC) 
    WHERE deleted_at IS NULL;

CREATE INDEX idx_audio_files_language_code 
    ON audio_files(language_code) 
    WHERE deleted_at IS NULL;

-- Transcriptions Indexes
CREATE INDEX idx_transcriptions_audio_file_id 
    ON transcriptions(audio_file_id);

CREATE INDEX idx_transcriptions_user_id 
    ON transcriptions(user_id) 
    WHERE deleted_at IS NULL;

CREATE INDEX idx_transcriptions_status 
    ON transcriptions(status) 
    WHERE deleted_at IS NULL;

CREATE INDEX idx_transcriptions_created_at 
    ON transcriptions(created_at DESC) 
    WHERE deleted_at IS NULL;

CREATE INDEX idx_transcriptions_language_detected 
    ON transcriptions(language_detected) 
    WHERE deleted_at IS NULL;

CREATE INDEX idx_transcriptions_user_status 
    ON transcriptions(user_id, status) 
    WHERE deleted_at IS NULL;

-- Transcription Segments Indexes
CREATE INDEX idx_transcription_segments_transcription_id 
    ON transcription_segments(transcription_id);

CREATE INDEX idx_transcription_segments_segment_index 
    ON transcription_segments(transcription_id, segment_index);

CREATE INDEX idx_transcription_segments_speaker_id 
    ON transcription_segments(transcription_id, speaker_id) 
    WHERE speaker_id IS NOT NULL;

CREATE INDEX idx_transcription_segments_start_time 
    ON transcription_segments(transcription_id, start_time_seconds);

-- Comments
COMMENT ON INDEX idx_audio_files_user_id IS 'Index for querying audio files by user';
COMMENT ON INDEX idx_audio_files_created_at IS 'Index for sorting audio files by creation date';
COMMENT ON INDEX idx_transcriptions_user_id IS 'Index for querying transcriptions by user';
COMMENT ON INDEX idx_transcriptions_status IS 'Index for querying transcriptions by status';
COMMENT ON INDEX idx_transcription_segments_transcription_id IS 'Index for querying segments by transcription';

-- rollback DROP INDEX IF EXISTS idx_audio_files_user_id;
-- rollback DROP INDEX IF EXISTS idx_audio_files_created_at;
-- rollback DROP INDEX IF EXISTS idx_audio_files_language_code;
-- rollback DROP INDEX IF EXISTS idx_transcriptions_audio_file_id;
-- rollback DROP INDEX IF EXISTS idx_transcriptions_user_id;
-- rollback DROP INDEX IF EXISTS idx_transcriptions_status;
-- rollback DROP INDEX IF EXISTS idx_transcriptions_created_at;
-- rollback DROP INDEX IF EXISTS idx_transcriptions_language_detected;
-- rollback DROP INDEX IF EXISTS idx_transcriptions_user_status;
-- rollback DROP INDEX IF EXISTS idx_transcription_segments_transcription_id;
-- rollback DROP INDEX IF EXISTS idx_transcription_segments_segment_index;
-- rollback DROP INDEX IF EXISTS idx_transcription_segments_speaker_id;
-- rollback DROP INDEX IF EXISTS idx_transcription_segments_start_time;
