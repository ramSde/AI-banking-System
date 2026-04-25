-- liquibase formatted sql

-- changeset rag-pipeline:2
-- comment: Create rag_contexts table for storing assembled contexts with source attribution

CREATE TABLE IF NOT EXISTS rag_contexts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    query_id UUID NOT NULL,
    assembled_context TEXT NOT NULL,
    token_count INTEGER NOT NULL,
    source_count INTEGER NOT NULL,
    sources JSONB NOT NULL,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_rag_contexts_query FOREIGN KEY (query_id) REFERENCES rag_queries(id) ON DELETE CASCADE
);

COMMENT ON TABLE rag_contexts IS 'Stores assembled RAG contexts with source attribution';
COMMENT ON COLUMN rag_contexts.id IS 'Unique identifier for the context';
COMMENT ON COLUMN rag_contexts.query_id IS 'Reference to the originating query';
COMMENT ON COLUMN rag_contexts.assembled_context IS 'Final assembled context text';
COMMENT ON COLUMN rag_contexts.token_count IS 'Total token count of assembled context';
COMMENT ON COLUMN rag_contexts.source_count IS 'Number of source documents included';
COMMENT ON COLUMN rag_contexts.sources IS 'JSON array of source documents with metadata';
COMMENT ON COLUMN rag_contexts.metadata IS 'Additional metadata about context assembly';
COMMENT ON COLUMN rag_contexts.created_at IS 'Timestamp when context was created';
COMMENT ON COLUMN rag_contexts.updated_at IS 'Timestamp when context was last updated';
COMMENT ON COLUMN rag_contexts.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN rag_contexts.version IS 'Optimistic locking version';

-- rollback DROP TABLE IF EXISTS rag_contexts;
