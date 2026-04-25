-- liquibase formatted sql

-- changeset document-ingestion:2
-- comment: Create document_chunks table for storing chunked document text with embeddings

CREATE TABLE IF NOT EXISTS document_chunks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id UUID NOT NULL,
    chunk_index INTEGER NOT NULL,
    chunk_text TEXT NOT NULL,
    token_count INTEGER NOT NULL,
    vector_id VARCHAR(500),
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_document_chunks_document FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE,
    CONSTRAINT chk_chunk_index_non_negative CHECK (chunk_index >= 0),
    CONSTRAINT chk_token_count_positive CHECK (token_count > 0),
    CONSTRAINT uq_document_chunk_index UNIQUE (document_id, chunk_index)
);

COMMENT ON TABLE document_chunks IS 'Stores chunked text from documents with references to vector embeddings';
COMMENT ON COLUMN document_chunks.id IS 'Unique identifier for the chunk';
COMMENT ON COLUMN document_chunks.document_id IS 'Reference to the parent document';
COMMENT ON COLUMN document_chunks.chunk_index IS 'Sequential index of this chunk within the document (0-based)';
COMMENT ON COLUMN document_chunks.chunk_text IS 'The actual text content of this chunk';
COMMENT ON COLUMN document_chunks.token_count IS 'Number of tokens in this chunk';
COMMENT ON COLUMN document_chunks.vector_id IS 'ID of the vector embedding in ChromaDB';
COMMENT ON COLUMN document_chunks.metadata IS 'Additional metadata in JSON format';
COMMENT ON COLUMN document_chunks.created_at IS 'Timestamp when the chunk was created';
COMMENT ON COLUMN document_chunks.updated_at IS 'Timestamp when the chunk was last updated';
COMMENT ON COLUMN document_chunks.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN document_chunks.version IS 'Version number for optimistic locking';

-- rollback DROP TABLE IF EXISTS document_chunks;
