-- liquibase formatted sql

-- changeset rag-pipeline:3
-- comment: Create rag_cache table for semantic caching

CREATE TABLE rag_cache (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    query_text TEXT NOT NULL,
    query_embedding VECTOR(1536) NOT NULL,
    cached_context_id UUID NOT NULL,
    hit_count INTEGER NOT NULL DEFAULT 1,
    last_hit_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_rag_cache_context FOREIGN KEY (cached_context_id) REFERENCES rag_contexts(id) ON DELETE CASCADE
);

COMMENT ON TABLE rag_cache IS 'Semantic cache for RAG queries based on embedding similarity';
COMMENT ON COLUMN rag_cache.id IS 'Primary key';
COMMENT ON COLUMN rag_cache.query_text IS 'Original cached query text';
COMMENT ON COLUMN rag_cache.query_embedding IS 'Vector embedding for similarity matching';
COMMENT ON COLUMN rag_cache.cached_context_id IS 'Reference to cached context';
COMMENT ON COLUMN rag_cache.hit_count IS 'Number of times this cache entry was used';
COMMENT ON COLUMN rag_cache.last_hit_at IS 'Timestamp of last cache hit';
COMMENT ON COLUMN rag_cache.expires_at IS 'Cache entry expiration timestamp';
COMMENT ON COLUMN rag_cache.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN rag_cache.updated_at IS 'Record last update timestamp';
COMMENT ON COLUMN rag_cache.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN rag_cache.version IS 'Optimistic locking version';

-- rollback DROP TABLE rag_cache;
