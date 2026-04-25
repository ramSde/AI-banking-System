-- liquibase formatted sql

-- changeset rag-pipeline:3
-- comment: Create rag_cache table for semantic caching

CREATE TABLE IF NOT EXISTS rag_cache (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    query_text TEXT NOT NULL,
    query_embedding BYTEA NOT NULL,
    cached_response JSONB NOT NULL,
    hit_count INTEGER NOT NULL DEFAULT 0,
    last_hit_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE rag_cache IS 'Semantic cache for RAG queries based on embedding similarity';
COMMENT ON COLUMN rag_cache.id IS 'Unique identifier for the cache entry';
COMMENT ON COLUMN rag_cache.query_text IS 'Original query text';
COMMENT ON COLUMN rag_cache.query_embedding IS 'Query embedding vector (serialized)';
COMMENT ON COLUMN rag_cache.cached_response IS 'Cached response data';
COMMENT ON COLUMN rag_cache.hit_count IS 'Number of times this cache entry was hit';
COMMENT ON COLUMN rag_cache.last_hit_at IS 'Timestamp of last cache hit';
COMMENT ON COLUMN rag_cache.expires_at IS 'Cache entry expiration timestamp';
COMMENT ON COLUMN rag_cache.created_at IS 'Timestamp when cache entry was created';
COMMENT ON COLUMN rag_cache.updated_at IS 'Timestamp when cache entry was last updated';
COMMENT ON COLUMN rag_cache.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN rag_cache.version IS 'Optimistic locking version';

-- rollback DROP TABLE IF EXISTS rag_cache;
