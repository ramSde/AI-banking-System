-- liquibase formatted sql

-- changeset chat:3
-- comment: Create message_feedback table for collecting user feedback

CREATE TABLE message_feedback (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id UUID NOT NULL,
    user_id UUID NOT NULL,
    rating INTEGER,
    feedback_type VARCHAR(50),
    comment TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_message_feedback_message FOREIGN KEY (message_id) REFERENCES chat_messages(id) ON DELETE CASCADE,
    CONSTRAINT chk_feedback_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT chk_feedback_type CHECK (feedback_type IN ('HELPFUL', 'NOT_HELPFUL', 'INACCURATE', 'INAPPROPRIATE', 'OTHER'))
);

COMMENT ON TABLE message_feedback IS 'Stores user feedback on chat messages';
COMMENT ON COLUMN message_feedback.id IS 'Primary key';
COMMENT ON COLUMN message_feedback.message_id IS 'Reference to chat message';
COMMENT ON COLUMN message_feedback.user_id IS 'User who provided feedback';
COMMENT ON COLUMN message_feedback.rating IS 'Rating from 1 to 5';
COMMENT ON COLUMN message_feedback.feedback_type IS 'Type of feedback';
COMMENT ON COLUMN message_feedback.comment IS 'Optional feedback comment';
COMMENT ON COLUMN message_feedback.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN message_feedback.updated_at IS 'Record last update timestamp';
COMMENT ON COLUMN message_feedback.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN message_feedback.version IS 'Optimistic locking version';

-- rollback DROP TABLE message_feedback;
