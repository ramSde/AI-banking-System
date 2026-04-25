# RAG Pipeline Service

## Overview

The RAG (Retrieval-Augmented Generation) Pipeline Service is a production-grade microservice responsible for intelligent document retrieval, reranking, context assembly, and source attribution. It serves as the core retrieval engine for the AI-powered banking platform, ensuring that AI responses are grounded in actual document content.

## Features

- **Vector Similarity Search**: ChromaDB-based semantic search with configurable similarity thresholds
- **Document Reranking**: Cross-encoder reranking for improved relevance scoring
- **Context Assembly**: Intelligent context window management with token counting
- **Source Attribution**: Complete tracking and attribution of document sources
- **Semantic Caching**: Redis-based embedding similarity cache to reduce costs
- **Query History**: Complete query tracking and analytics
- **Multi-document Retrieval**: Retrieve and rank documents from multiple sources

## Prerequisites

- Java 17
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 16
- Redis 7
- Apache Kafka 3.x
- ChromaDB

## Technology Stack

- **Framework**: Spring Boot 3.2.5
- **Language**: Java 17
- **Database**: PostgreSQL 16 (Liquibase migrations)
- **Cache**: Redis 7
- **Messaging**: Apache Kafka
- **Vector Database**: ChromaDB
- **AI**: Spring AI + OpenAI
- **Security**: Spring Security 6 + JWT
- **Observability**: Micrometer + Prometheus + OpenTelemetry

## Quick Start

### 1. Environment Setup

```bash
cp .env.example .env
# Edit .env with your configuration
```

### 2. Start Infrastructure

```bash
docker-compose up -d postgres redis kafka chromadb
```

### 3. Build and Run

```bash
mvn clean package
java -jar target/rag-pipeline-service-1.0.0.jar --spring.profiles.active=dev
```

## API Endpoints

| Method | Path | Auth | Role | Description |
|--------|------|------|------|-------------|
| POST | `/v1/rag/retrieve` | Yes | USER | Retrieve relevant documents |
| POST | `/v1/rag/rerank` | Yes | USER | Rerank retrieved documents |
| POST | `/v1/rag/assemble` | Yes | USER | Assemble context from documents |
| GET | `/v1/rag/queries/{id}` | Yes | USER | Get query details |
| GET | `/v1/rag/queries` | Yes | USER | Get query history |
| GET | `/v1/rag/cache/stats` | Yes | USER | Get cache statistics |
| DELETE | `/v1/rag/cache` | Yes | ADMIN | Clear semantic cache |

## Architecture

### RAG Pipeline Flow

1. **Query Reception**: User submits query via REST API
2. **Embedding Generation**: Query converted to vector embedding
3. **Semantic Cache Check**: Check Redis for similar cached queries
4. **Vector Search**: ChromaDB similarity search if cache miss
5. **Reranking**: Cross-encoder reranking for relevance
6. **Context Assembly**: Assemble context within token limits
7. **Source Attribution**: Track and return document sources
8. **Cache Update**: Store result in semantic cache

### Dependencies

- **Document Ingestion Service**: Vector search and document metadata
- **Redis**: Semantic caching and rate limiting
- **ChromaDB**: Vector similarity search
- **OpenAI API**: Embedding generation

## Configuration

See `.env.example` for all configuration options.

Key configurations:
- `RAG_TOP_K`: Number of documents to retrieve (default: 10)
- `RAG_SIMILARITY_THRESHOLD`: Minimum similarity score (default: 0.7)
- `RAG_MAX_CONTEXT_TOKENS`: Maximum context window (default: 4000)
- `RAG_CACHE_TTL`: Semantic cache TTL in seconds (default: 3600)
- `RAG_CACHE_SIMILARITY_THRESHOLD`: Cache hit threshold (default: 0.95)

## Database Schema

### rag_queries
Stores query history and metadata.

### rag_contexts
Stores assembled contexts with source attribution.

### rag_cache
Stores semantic cache entries with embeddings.

## Deployment

### Docker Build

```bash
docker build -t rag-pipeline-service:latest .
```

### Kubernetes/OpenShift

```bash
kubectl apply -f k8s/
```

## Monitoring

- **Health**: `/actuator/health`
- **Metrics**: `/actuator/prometheus`
- **Traces**: Exported to Jaeger/Tempo

## Known Limitations

1. **Context Window**: Limited by model's maximum context length
2. **Reranking Latency**: Cross-encoder reranking adds latency
3. **Cache Accuracy**: Semantic cache may have false positives
4. **Vector Search**: Accuracy depends on embedding quality

## Future Improvements

- Hybrid search (vector + keyword)
- Multi-vector retrieval
- Query expansion
- Relevance feedback learning
- Advanced reranking models

## License

Proprietary - Banking Platform
