-- liquibase formatted sql

-- changeset rag-pipeline:1
-- comment: Create rag_queries table for query history and metadata

CREATE TABLE rag_queries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    session_id UUID,
    query_text TEXT NOT NULL,
    query_embedding VECTOR(1536),
    top_k INTEGER NOT NULL DEFAULT 10,
    similarity_threshold DECIMAL(5,4) NOT NULL DEFAULT 0.7000,
    rerank_enabled BOOLEAN NOT NULL DEFAULT true,
    max_context_tokens INTEGER NOT NULL DEFAULT 4000,
    retrieved_count INTEGER NOT NULL DEFAULT 0,
    final_count INTEGER NOT NULL DEFAULT 0,
    cache_hit BOOLEAN NOT NULL DEFAULT false,
    retrieval_latency_ms BIGINT,
    rerank_latency_ms BIGINT,
    total_latency_ms BIGINT,
    trace_id VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE rag_queries IS 'Stores RAG query history and metadata for analytics and debugging';
COMMENT ON COLUMN rag_queries.id IS 'Primary key';
COMMENT ON COLUMN rag_queries.user_id IS 'User who submitted the query';
COMMENT ON COLUMN rag_queries.session_id IS 'Session identifier for multi-turn conversations';
COMMENT ON COLUMN rag_queries.query_text IS 'Original query text';
COMMENT ON COLUMN rag_queries.query_embedding IS 'Vector embedding of the query (1536 dimensions for OpenAI)';
COMMENT ON COLUMN rag_queries.top_k IS 'Number of documents requested for retrieval';
COMMENT ON COLUMN rag_queries.similarity_threshold IS 'Minimum similarity score for retrieval';
COMMENT ON COLUMN rag_queries.rerank_enabled IS 'Whether reranking was enabled';
COMMENT ON COLUMN rag_queries.max_context_tokens IS 'Maximum context window size in tokens';
COMMENT ON COLUMN rag_queries.retrieved_count IS 'Number of documents retrieved from vector search';
COMMENT ON COLUMN rag_queries.final_count IS 'Number of documents after reranking and filtering';
COMMENT ON COLUMN rag_queries.cache_hit IS 'Whether result was served from semantic cache';
COMMENT ON COLUMN rag_queries.retrieval_latency_ms IS 'Vector search latency in milliseconds';
COMMENT ON COLUMN rag_queries.rerank_latency_ms IS 'Reranking latency in milliseconds';
COMMENT ON COLUMN rag_queries.total_latency_ms IS 'Total end-to-end latency in milliseconds';
COMMENT ON COLUMN rag_queries.trace_id IS 'Distributed tracing identifier';
COMMENT ON COLUMN rag_queries.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN rag_queries.updated_at IS 'Record last update timestamp';
COMMENT ON COLUMN rag_queries.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN rag_queries.version IS 'Optimistic locking version';

-- rollback DROP TABLE rag_queries;
