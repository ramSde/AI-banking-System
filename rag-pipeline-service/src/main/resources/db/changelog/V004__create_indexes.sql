-- liquibase formatted sql

-- changeset rag-pipeline:4
-- comment: Create indexes for performance optimization

-- Indexes on rag_queries
CREATE INDEX idx_rag_queries_user_id ON rag_queries(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_rag_queries_session_id ON rag_queries(session_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_rag_queries_created_at ON rag_queries(created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_rag_queries_trace_id ON rag_queries(trace_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_rag_queries_cache_hit ON rag_queries(cache_hit) WHERE deleted_at IS NULL;

-- Vector similarity index on rag_queries (for finding similar past queries)
CREATE INDEX idx_rag_queries_embedding ON rag_queries USING ivfflat (query_embedding vector_cosine_ops) WITH (lists = 100) WHERE deleted_at IS NULL;

-- Indexes on rag_contexts
CREATE INDEX idx_rag_contexts_query_id ON rag_contexts(query_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_rag_contexts_created_at ON rag_contexts(created_at DESC) WHERE deleted_at IS NULL;

-- GIN index on sources JSONB for efficient querying
CREATE INDEX idx_rag_contexts_sources ON rag_contexts USING GIN (sources) WHERE deleted_at IS NULL;

-- Indexes on rag_cache
CREATE INDEX idx_rag_cache_expires_at ON rag_cache(expires_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_rag_cache_last_hit_at ON rag_cache(last_hit_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_rag_cache_cached_context_id ON rag_cache(cached_context_id) WHERE deleted_at IS NULL;

-- Vector similarity index on rag_cache (for semantic cache lookup)
CREATE INDEX idx_rag_cache_embedding ON rag_cache USING ivfflat (query_embedding vector_cosine_ops) WITH (lists = 100) WHERE deleted_at IS NULL AND expires_at > NOW();

-- Composite indexes for common query patterns
CREATE INDEX idx_rag_queries_user_created ON rag_queries(user_id, created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_rag_queries_session_created ON rag_queries(session_id, created_at DESC) WHERE deleted_at IS NULL;

-- rollback DROP INDEX IF EXISTS idx_rag_queries_user_id;
-- rollback DROP INDEX IF EXISTS idx_rag_queries_session_id;
-- rollback DROP INDEX IF EXISTS idx_rag_queries_created_at;
-- rollback DROP INDEX IF EXISTS idx_rag_queries_trace_id;
-- rollback DROP INDEX IF EXISTS idx_rag_queries_cache_hit;
-- rollback DROP INDEX IF EXISTS idx_rag_queries_embedding;
-- rollback DROP INDEX IF EXISTS idx_rag_contexts_query_id;
-- rollback DROP INDEX IF EXISTS idx_rag_contexts_created_at;
-- rollback DROP INDEX IF EXISTS idx_rag_contexts_sources;
-- rollback DROP INDEX IF EXISTS idx_rag_cache_expires_at;
-- rollback DROP INDEX IF EXISTS idx_rag_cache_last_hit_at;
-- rollback DROP INDEX IF EXISTS idx_rag_cache_cached_context_id;
-- rollback DROP INDEX IF EXISTS idx_rag_cache_embedding;
-- rollback DROP INDEX IF EXISTS idx_rag_queries_user_created;
-- rollback DROP INDEX IF EXISTS idx_rag_queries_session_created;
