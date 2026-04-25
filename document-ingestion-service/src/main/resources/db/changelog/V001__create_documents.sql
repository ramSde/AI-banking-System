-- liquibase formatted sql

-- changeset document-ingestion:1
-- comment: Create documents table for storing document metadata

CREATE TABLE IF NOT EXISTS documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    original_filename VARCHAR(500) NOT NULL,
    storage_key VARCHAR(1000) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    processing_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    total_chunks INTEGER DEFAULT 0,
    extracted_text TEXT,
    error_message TEXT,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_file_size_positive CHECK (file_size_bytes > 0),
    CONSTRAINT chk_total_chunks_non_negative CHECK (total_chunks >= 0),
    CONSTRAINT chk_processing_status CHECK (processing_status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'))
);

COMMENT ON TABLE documents IS 'Stores metadata for uploaded documents including processing status and storage location';
COMMENT ON COLUMN documents.id IS 'Unique identifier for the document';
COMMENT ON COLUMN documents.user_id IS 'ID of the user who uploaded the document';
COMMENT ON COLUMN documents.original_filename IS 'Original filename as uploaded by user';
COMMENT ON COLUMN documents.storage_key IS 'Object storage key (MinIO/S3) for retrieving the file';
COMMENT ON COLUMN documents.mime_type IS 'MIME type of the uploaded file';
COMMENT ON COLUMN documents.file_size_bytes IS 'Size of the file in bytes';
COMMENT ON COLUMN documents.document_type IS 'Type of document (INVOICE, RECEIPT, STATEMENT, etc.)';
COMMENT ON COLUMN documents.processing_status IS 'Current processing status (PENDING, PROCESSING, COMPLETED, FAILED)';
COMMENT ON COLUMN documents.total_chunks IS 'Total number of chunks created from this document';
COMMENT ON COLUMN documents.extracted_text IS 'Full extracted text from the document';
COMMENT ON COLUMN documents.error_message IS 'Error message if processing failed';
COMMENT ON COLUMN documents.metadata IS 'Additional metadata in JSON format';
COMMENT ON COLUMN documents.created_at IS 'Timestamp when the document was uploaded';
COMMENT ON COLUMN documents.updated_at IS 'Timestamp when the document was last updated';
COMMENT ON COLUMN documents.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN documents.version IS 'Version number for optimistic locking';

-- rollback DROP TABLE IF EXISTS documents;
