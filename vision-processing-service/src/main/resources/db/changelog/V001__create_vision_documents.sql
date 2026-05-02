-- liquibase formatted sql

-- changeset vision:1
-- comment: Create vision_documents table

CREATE TABLE vision_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    storage_key VARCHAR(500) NOT NULL,
    processing_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    confidence_score DECIMAL(5,2),
    error_message TEXT,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE vision_documents IS 'Stores metadata for uploaded documents processed by vision service';
COMMENT ON COLUMN vision_documents.id IS 'Unique document identifier';
COMMENT ON COLUMN vision_documents.user_id IS 'Owner user ID';
COMMENT ON COLUMN vision_documents.document_type IS 'Type of document (RECEIPT, INVOICE, CHECK, etc.)';
COMMENT ON COLUMN vision_documents.original_filename IS 'Original uploaded filename';
COMMENT ON COLUMN vision_documents.file_size IS 'File size in bytes';
COMMENT ON COLUMN vision_documents.mime_type IS 'MIME type of uploaded file';
COMMENT ON COLUMN vision_documents.storage_key IS 'MinIO/S3 storage key';
COMMENT ON COLUMN vision_documents.processing_status IS 'Current processing status (PENDING, PROCESSING, COMPLETED, FAILED)';
COMMENT ON COLUMN vision_documents.confidence_score IS 'Overall OCR confidence score (0-100)';
COMMENT ON COLUMN vision_documents.error_message IS 'Error message if processing failed';
COMMENT ON COLUMN vision_documents.metadata IS 'Additional metadata as JSON';
COMMENT ON COLUMN vision_documents.created_at IS 'Document upload timestamp';
COMMENT ON COLUMN vision_documents.updated_at IS 'Last update timestamp';
COMMENT ON COLUMN vision_documents.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN vision_documents.version IS 'Optimistic locking version';

-- rollback DROP TABLE vision_documents;
