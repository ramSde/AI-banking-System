-- liquibase formatted sql

-- changeset rag-pipeline:4
-- comment: Create indexes for performance optimization

-- Indexes for rag_queries table
CREATE INDEX IF NOT EXISTS idx_rag_queries_user_id ON rag_queries(user_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_rag_queries_status ON rag_queries(status) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_rag_queries_created_at ON rag_queries(created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_rag_queries_trace_id ON rag_queries(trace_id) WHERE deleted_at IS NULL AND trace_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_rag_queries_session_id ON rag_queries(session_id) WHERE deleted_at IS NULL AND session_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_rag_queries_user_created ON rag_queries(user_id, created_at DESC) WHERE deleted_at IS NULL;

-- Indexes for rag_contexts table
CREATE INDEX IF NOT EXISTS idx_rag_contexts_query_id ON rag_contexts(query_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_rag_contexts_created_at ON rag_contexts(created_at DESC) WHERE deleted_at IS NULL;

-- Indexes for rag_cache table
CREATE INDEX IF NOT EXISTS idx_rag_cache_expires_at ON rag_cache(expires_at) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_rag_cache_created_at ON rag_cache(created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_rag_cache_hit_count ON rag_cache(hit_count DESC) WHERE deleted_at IS NULL;

-- GIN index for JSONB columns
CREATE INDEX IF NOT EXISTS idx_rag_contexts_sources ON rag_contexts USING GIN(sources) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_rag_contexts_metadata ON rag_contexts USING GIN(metadata) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_rag_cache_response ON rag_cache USING GIN(cached_response) WHERE deleted_at IS NULL;

-- rollback DROP INDEX IF EXISTS idx_rag_queries_user_id;
-- rollback DROP INDEX IF EXISTS idx_rag_queries_status;
-- rollback DROP INDEX IF EXISTS idx_rag_queries_created_at;
-- rollback DROP INDEX IF EXISTS idx_rag_queries_trace_id;
-- rollback DROP INDEX IF EXISTS idx_rag_queries_session_id;
-- rollback DROP INDEX IF EXISTS idx_rag_queries_user_created;
-- rollback DROP INDEX IF EXISTS idx_rag_contexts_query_id;
-- rollback DROP INDEX IF EXISTS idx_rag_contexts_created_at;
-- rollback DROP INDEX IF EXISTS idx_rag_cache_expires_at;
-- rollback DROP INDEX IF EXISTS idx_rag_cache_created_at;
-- rollback DROP INDEX IF EXISTS idx_rag_cache_hit_count;
-- rollback DROP INDEX IF EXISTS idx_rag_contexts_sources;
-- rollback DROP INDEX IF EXISTS idx_rag_contexts_metadata;
-- rollback DROP INDEX IF EXISTS idx_rag_cache_response;
