-- liquibase formatted sql

-- changeset rag-pipeline:1
-- comment: Create rag_queries table for storing query history and metadata

CREATE TABLE IF NOT EXISTS rag_queries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    query_text TEXT NOT NULL,
    query_embedding BYTEA,
    top_k INTEGER NOT NULL DEFAULT 10,
    similarity_threshold DECIMAL(5,4) NOT NULL DEFAULT 0.7,
    reranking_enabled BOOLEAN NOT NULL DEFAULT true,
    cache_hit BOOLEAN NOT NULL DEFAULT false,
    retrieval_latency_ms BIGINT,
    reranking_latency_ms BIGINT,
    total_latency_ms BIGINT,
    results_count INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    trace_id VARCHAR(255),
    session_id VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE rag_queries IS 'Stores RAG query history and execution metadata';
COMMENT ON COLUMN rag_queries.id IS 'Unique identifier for the query';
COMMENT ON COLUMN rag_queries.user_id IS 'User who submitted the query';
COMMENT ON COLUMN rag_queries.query_text IS 'Original query text';
COMMENT ON COLUMN rag_queries.query_embedding IS 'Query embedding vector (serialized)';
COMMENT ON COLUMN rag_queries.top_k IS 'Number of documents to retrieve';
COMMENT ON COLUMN rag_queries.similarity_threshold IS 'Minimum similarity score threshold';
COMMENT ON COLUMN rag_queries.reranking_enabled IS 'Whether reranking was enabled';
COMMENT ON COLUMN rag_queries.cache_hit IS 'Whether result was served from cache';
COMMENT ON COLUMN rag_queries.retrieval_latency_ms IS 'Time taken for retrieval in milliseconds';
COMMENT ON COLUMN rag_queries.reranking_latency_ms IS 'Time taken for reranking in milliseconds';
COMMENT ON COLUMN rag_queries.total_latency_ms IS 'Total processing time in milliseconds';
COMMENT ON COLUMN rag_queries.results_count IS 'Number of results returned';
COMMENT ON COLUMN rag_queries.status IS 'Query status: PENDING, COMPLETED, FAILED';
COMMENT ON COLUMN rag_queries.error_message IS 'Error message if query failed';
COMMENT ON COLUMN rag_queries.trace_id IS 'Distributed tracing ID';
COMMENT ON COLUMN rag_queries.session_id IS 'User session ID';
COMMENT ON COLUMN rag_queries.created_at IS 'Timestamp when query was created';
COMMENT ON COLUMN rag_queries.updated_at IS 'Timestamp when query was last updated';
COMMENT ON COLUMN rag_queries.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN rag_queries.version IS 'Optimistic locking version';

-- rollback DROP TABLE IF EXISTS rag_queries;
