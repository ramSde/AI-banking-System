-- liquibase formatted sql

-- changeset document-ingestion:3
-- comment: Create indexes for optimized query performance

-- Index on user_id for filtering documents by user
CREATE INDEX IF NOT EXISTS idx_documents_user_id ON documents(user_id) WHERE deleted_at IS NULL;

-- Index on processing_status for filtering by status
CREATE INDEX IF NOT EXISTS idx_documents_processing_status ON documents(processing_status) WHERE deleted_at IS NULL;

-- Index on document_type for filtering by type
CREATE INDEX IF NOT EXISTS idx_documents_document_type ON documents(document_type) WHERE deleted_at IS NULL;

-- Composite index for user + status queries
CREATE INDEX IF NOT EXISTS idx_documents_user_status ON documents(user_id, processing_status) WHERE deleted_at IS NULL;

-- Index on created_at for time-based queries
CREATE INDEX IF NOT EXISTS idx_documents_created_at ON documents(created_at DESC) WHERE deleted_at IS NULL;

-- Index on storage_key for lookups
CREATE INDEX IF NOT EXISTS idx_documents_storage_key ON documents(storage_key) WHERE deleted_at IS NULL;

-- Index on document_id for chunk lookups
CREATE INDEX IF NOT EXISTS idx_document_chunks_document_id ON document_chunks(document_id) WHERE deleted_at IS NULL;

-- Index on vector_id for vector store synchronization
CREATE INDEX IF NOT EXISTS idx_document_chunks_vector_id ON document_chunks(vector_id) WHERE deleted_at IS NULL AND vector_id IS NOT NULL;

-- Composite index for document + chunk_index queries
CREATE INDEX IF NOT EXISTS idx_document_chunks_doc_index ON document_chunks(document_id, chunk_index) WHERE deleted_at IS NULL;

-- GIN index on metadata JSONB columns for JSON queries
CREATE INDEX IF NOT EXISTS idx_documents_metadata_gin ON documents USING GIN (metadata) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_document_chunks_metadata_gin ON document_chunks USING GIN (metadata) WHERE deleted_at IS NULL;

-- rollback DROP INDEX IF EXISTS idx_documents_user_id;
-- rollback DROP INDEX IF EXISTS idx_documents_processing_status;
-- rollback DROP INDEX IF EXISTS idx_documents_document_type;
-- rollback DROP INDEX IF EXISTS idx_documents_user_status;
-- rollback DROP INDEX IF EXISTS idx_documents_created_at;
-- rollback DROP INDEX IF EXISTS idx_documents_storage_key;
-- rollback DROP INDEX IF EXISTS idx_document_chunks_document_id;
-- rollback DROP INDEX IF EXISTS idx_document_chunks_vector_id;
-- rollback DROP INDEX IF EXISTS idx_document_chunks_doc_index;
-- rollback DROP INDEX IF EXISTS idx_documents_metadata_gin;
-- rollback DROP INDEX IF EXISTS idx_document_chunks_metadata_gin;
