-- liquibase formatted sql

-- changeset rag-pipeline:2
-- comment: Create rag_contexts table for assembled contexts with source attribution

CREATE TABLE rag_contexts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    query_id UUID NOT NULL,
    assembled_context TEXT NOT NULL,
    total_tokens INTEGER NOT NULL,
    document_count INTEGER NOT NULL,
    sources JSONB NOT NULL,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_rag_contexts_query FOREIGN KEY (query_id) REFERENCES rag_queries(id) ON DELETE CASCADE
);

COMMENT ON TABLE rag_contexts IS 'Stores assembled contexts with complete source attribution';
COMMENT ON COLUMN rag_contexts.id IS 'Primary key';
COMMENT ON COLUMN rag_contexts.query_id IS 'Reference to the originating query';
COMMENT ON COLUMN rag_contexts.assembled_context IS 'Final assembled context text';
COMMENT ON COLUMN rag_contexts.total_tokens IS 'Total token count of assembled context';
COMMENT ON COLUMN rag_contexts.document_count IS 'Number of documents included in context';
COMMENT ON COLUMN rag_contexts.sources IS 'JSON array of source documents with metadata (document_id, chunk_id, score, title, page)';
COMMENT ON COLUMN rag_contexts.metadata IS 'Additional metadata (reranking scores, filtering decisions)';
COMMENT ON COLUMN rag_contexts.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN rag_contexts.updated_at IS 'Record last update timestamp';
COMMENT ON COLUMN rag_contexts.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN rag_contexts.version IS 'Optimistic locking version';

-- rollback DROP TABLE rag_contexts;
