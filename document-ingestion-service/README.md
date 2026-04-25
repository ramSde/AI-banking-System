# Document Ingestion Service

## Overview

The Document Ingestion Service is a production-grade microservice responsible for processing documents (PDFs and images), extracting text, chunking content, generating embeddings, and storing vectors in ChromaDB for semantic search capabilities. It serves as the foundation for the RAG (Retrieval-Augmented Generation) pipeline in the banking platform.

## Features

- **Multi-format Support**: PDF and image (PNG, JPEG) document processing
- **Text Extraction**: Apache PDFBox for PDFs, Tesseract OCR for images
- **Semantic Chunking**: Intelligent text chunking with configurable size and overlap
- **Vector Embeddings**: OpenAI embeddings via Spring AI
- **Vector Storage**: ChromaDB integration for similarity search
- **Object Storage**: MinIO for document file storage with pre-signed URLs
- **Event-Driven**: Kafka-based asynchronous processing
- **Security**: JWT authentication, role-based access control
- **Observability**: Structured JSON logging, Prometheus metrics, distributed tracing
- **Resilience**: Circuit breakers, retries, fallback mechanisms

## Prerequisites

- Java 17
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 16
- Redis 7
- Apache Kafka 3.x
- MinIO
- ChromaDB
- Tesseract OCR

## Local Setup

### 1. Environment Configuration

Copy the example environment file and configure:

```bash
cp .env.example .env
```

Edit `.env` with your local configuration values.

### 2. Start Infrastructure Services

```bash
docker-compose up -d postgres redis kafka minio chromadb
```

### 3. Install Tesseract OCR

**macOS:**
```bash
brew install tesseract
```

**Ubuntu/Debian:**
```bash
sudo apt-get install tesseract-ocr tesseract-ocr-eng
```

**Windows:**
Download from: https://github.com/UB-Mannheim/tesseract/wiki

### 4. Build the Service

```bash
mvn clean package
```

### 5. Run the Service

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Or run the JAR:

```bash
java -jar target/document-ingestion-service-1.0.0.jar --spring.profiles.active=dev
```

## API Endpoints

### Document Management

| Method | Path | Auth | Role | Description |
|--------|------|------|------|-------------|
| POST | `/v1/documents/upload` | Yes | USER | Upload a document for processing |
| GET | `/v1/documents/{id}` | Yes | USER | Get document by ID |
| GET | `/v1/documents` | Yes | USER | Get user's documents (paginated) |
| GET | `/v1/documents/status/{status}` | Yes | USER | Get documents by processing status |
| GET | `/v1/documents/type/{type}` | Yes | USER | Get documents by document type |
| POST | `/v1/documents/search` | Yes | USER | Search documents by semantic similarity |
| GET | `/v1/documents/stats` | Yes | USER | Get document statistics |
| GET | `/v1/documents/{id}/download-url` | Yes | USER | Get pre-signed download URL |
| DELETE | `/v1/documents/{id}` | Yes | USER | Soft delete a document |

### Admin Endpoints

| Method | Path | Auth | Role | Description |
|--------|------|------|------|-------------|
| GET | `/v1/admin/documents/status/{status}` | Yes | ADMIN | Get all documents by status |
| POST | `/v1/admin/documents/{id}/reprocess` | Yes | ADMIN | Trigger document reprocessing |

### Actuator Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/actuator/health` | No | Health check |
| GET | `/actuator/health/liveness` | No | Liveness probe |
| GET | `/actuator/health/readiness` | No | Readiness probe |
| GET | `/actuator/metrics` | No | Metrics |
| GET | `/actuator/prometheus` | No | Prometheus metrics |

## Environment Variables

| Variable | Default | Description | Required |
|----------|---------|-------------|----------|
| `SERVER_PORT` | 8012 | Server port | No |
| `SPRING_PROFILES_ACTIVE` | dev | Active profile (dev/staging/prod) | No |
| `DATABASE_URL` | jdbc:postgresql://localhost:5432/banking_documents | PostgreSQL connection URL | Yes |
| `DATABASE_USERNAME` | admin | Database username | Yes |
| `DATABASE_PASSWORD` | admin | Database password | Yes |
| `REDIS_HOST` | localhost | Redis host | Yes |
| `REDIS_PORT` | 6379 | Redis port | Yes |
| `KAFKA_BOOTSTRAP_SERVERS` | localhost:9092 | Kafka bootstrap servers | Yes |
| `MINIO_ENDPOINT` | http://localhost:9000 | MinIO endpoint | Yes |
| `MINIO_ACCESS_KEY` | admin | MinIO access key | Yes |
| `MINIO_SECRET_KEY` | password | MinIO secret key | Yes |
| `OPENAI_API_KEY` | - | OpenAI API key for embeddings | Yes |
| `CHROMA_HOST` | localhost | ChromaDB host | Yes |
| `CHROMA_PORT` | 8000 | ChromaDB port | Yes |
| `JWT_SECRET` | - | JWT signing secret (min 256 bits) | Yes |
| `DOCUMENT_CHUNK_SIZE` | 1000 | Chunk size in tokens | No |
| `DOCUMENT_CHUNK_OVERLAP` | 200 | Chunk overlap in tokens | No |

## Sample Requests

### Upload Document

```bash
curl -X POST http://localhost:8012/api/v1/documents/upload \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/document.pdf" \
  -F "documentType=INVOICE"
```

### Get Document

```bash
curl -X GET http://localhost:8012/api/v1/documents/{documentId} \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Search Documents

```bash
curl -X POST http://localhost:8012/api/v1/documents/search \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "invoice payment details",
    "topK": 5,
    "similarityThreshold": 0.7
  }'
```

### Get Document Stats

```bash
curl -X GET http://localhost:8012/api/v1/documents/stats \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Architecture

### Document Processing Flow

1. **Upload**: User uploads document via multipart/form-data
2. **Storage**: File stored in MinIO with generated storage key
3. **Metadata**: Document metadata saved to PostgreSQL with PENDING status
4. **Async Processing**: Kafka event triggers background processing
5. **Text Extraction**: PDFBox (PDF) or Tesseract OCR (images) extracts text
6. **Chunking**: Text split into semantic chunks with overlap
7. **Embeddings**: OpenAI generates vector embeddings for each chunk
8. **Vector Storage**: Embeddings stored in ChromaDB with metadata
9. **Completion**: Document status updated to COMPLETED, event published

### Technology Stack

- **Framework**: Spring Boot 3.2.5
- **Language**: Java 17
- **Database**: PostgreSQL 16 (Liquibase migrations)
- **Cache**: Redis 7
- **Messaging**: Apache Kafka
- **Object Storage**: MinIO
- **Vector Database**: ChromaDB
- **AI**: Spring AI + OpenAI
- **PDF Processing**: Apache PDFBox 3.0.2
- **OCR**: Tesseract 5.11.0 (via Tess4j)
- **Security**: Spring Security 6 + JWT
- **Observability**: Micrometer + Prometheus + OpenTelemetry

## Kafka Topics

| Topic | Type | Description |
|-------|------|-------------|
| `banking.documents.upload-requested` | Consumed | Document upload requested |
| `banking.documents.processed` | Produced | Document processing completed |
| `banking.documents.processing-failed` | Produced | Document processing failed |
| `banking.documents.upload-requested.dlq` | DLQ | Dead letter queue |
| `banking.documents.upload-requested.retry` | Retry | Retry topic |

## Database Schema

### documents

| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| user_id | UUID | User who uploaded the document |
| original_filename | VARCHAR(500) | Original filename |
| storage_key | VARCHAR(1000) | MinIO storage key |
| mime_type | VARCHAR(100) | MIME type |
| file_size_bytes | BIGINT | File size in bytes |
| document_type | VARCHAR(50) | Document type enum |
| processing_status | VARCHAR(50) | Processing status enum |
| total_chunks | INTEGER | Number of chunks created |
| extracted_text | TEXT | Full extracted text |
| error_message | TEXT | Error message if failed |
| metadata | JSONB | Additional metadata |
| created_at | TIMESTAMPTZ | Creation timestamp |
| updated_at | TIMESTAMPTZ | Last update timestamp |
| deleted_at | TIMESTAMPTZ | Soft delete timestamp |
| version | BIGINT | Optimistic locking version |

### document_chunks

| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| document_id | UUID | Foreign key to documents |
| chunk_index | INTEGER | Sequential chunk index |
| chunk_text | TEXT | Chunk text content |
| token_count | INTEGER | Token count |
| vector_id | VARCHAR(500) | ChromaDB vector ID |
| metadata | JSONB | Additional metadata |
| created_at | TIMESTAMPTZ | Creation timestamp |
| updated_at | TIMESTAMPTZ | Last update timestamp |
| deleted_at | TIMESTAMPTZ | Soft delete timestamp |
| version | BIGINT | Optimistic locking version |

## Deployment

### Docker Build

```bash
docker build -t document-ingestion-service:latest .
```

### Kubernetes/OpenShift Deployment

```bash
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/hpa.yaml
```

### Health Checks

- **Liveness**: `GET /api/actuator/health/liveness`
- **Readiness**: `GET /api/actuator/health/readiness`

## Monitoring

### Metrics

Prometheus metrics available at `/api/actuator/prometheus`:

- `http_server_requests_seconds` - HTTP request latency
- `jvm_memory_used_bytes` - JVM memory usage
- `hikaricp_connections_active` - Active database connections
- `kafka_consumer_fetch_manager_records_consumed_total` - Kafka messages consumed

### Logging

Structured JSON logs with fields:
- `timestamp` - ISO 8601 UTC timestamp
- `level` - Log level
- `service` - Service name
- `traceId` - Distributed trace ID
- `spanId` - Span ID
- `userId` - User ID (if authenticated)
- `message` - Log message

## Known Limitations

1. **OCR Accuracy**: Tesseract OCR accuracy depends on image quality
2. **Large Files**: Files > 10MB rejected (configurable)
3. **Supported Formats**: Only PDF and images (PNG, JPEG)
4. **Embedding Costs**: OpenAI API calls incur costs per document
5. **Processing Time**: Large documents may take several minutes to process

## Troubleshooting

### Tesseract Not Found

Ensure Tesseract is installed and `TESSERACT_DATA_PATH` points to tessdata directory.

### MinIO Connection Failed

Verify MinIO is running and accessible at configured endpoint.

### ChromaDB Connection Failed

Ensure ChromaDB is running on configured host and port.

### OpenAI API Errors

Check API key validity and account quota limits.

## Future Improvements

- Support for additional document formats (DOCX, TXT, HTML)
- Multi-language OCR support
- Batch document processing
- Document versioning
- Advanced metadata extraction
- Custom embedding models (Ollama integration)
- pgvector as alternative to ChromaDB

## License

Proprietary - Banking Platform

## Contact

For issues or questions, contact the platform team.
