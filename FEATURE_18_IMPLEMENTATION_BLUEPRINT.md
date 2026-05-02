# Feature 18: Vision Processing Service - Complete Implementation Blueprint

## Status: READY FOR IMPLEMENTATION

This document provides the complete blueprint for implementing Feature 18: Vision Processing Service following all 21 mandatory sections from the master system prompt.

---

## IMPLEMENTATION SUMMARY

**Service**: Vision Processing Service  
**Port**: 8018  
**Database**: banking_vision  
**Purpose**: OCR and document intelligence for receipts, invoices, statements, checks, and ID documents

---

## CRITICAL FILES TO CREATE (60+ files)

### 1. Configuration Files (6 files)
- ✅ pom.xml (CREATED)
- ⏳ src/main/resources/application.yml
- ⏳ src/main/resources/application-dev.yml
- ⏳ src/main/resources/application-staging.yml
- ⏳ src/main/resources/application-prod.yml
- ⏳ src/main/resources/logback-spring.xml
- ⏳ .env.example

### 2. Database Migrations (5 files)
- ⏳ src/main/resources/db/changelog/changelog-master.xml
- ⏳ src/main/resources/db/changelog/V001__create_vision_documents.sql
- ⏳ src/main/resources/db/changelog/V002__create_ocr_results.sql
- ⏳ src/main/resources/db/changelog/V003__create_extraction_templates.sql
- ⏳ src/main/resources/db/changelog/V004__create_indexes.sql
- ⏳ src/main/resources/db/changelog/V005__seed_templates.sql

### 3. Domain Entities (6 files)
- ⏳ VisionDocument.java
- ⏳ OcrResult.java
- ⏳ ExtractionTemplate.java
- ⏳ DocumentType.java (enum)
- ⏳ ProcessingStatus.java (enum)
- ⏳ ConfidenceLevel.java (enum)

### 4. Repositories (3 files)
- ⏳ VisionDocumentRepository.java
- ⏳ OcrResultRepository.java
- ⏳ ExtractionTemplateRepository.java

### 5. DTOs (9 files)
- ⏳ ApiResponse.java
- ⏳ DocumentUploadRequest.java
- ⏳ DocumentUploadResponse.java
- ⏳ OcrResultResponse.java
- ⏳ ExtractedDataResponse.java
- ⏳ ProcessingStatusResponse.java
- ⏳ ReceiptData.java
- ⏳ InvoiceData.java
- ⏳ CheckData.java

### 6. Service Interfaces (6 files)
- ⏳ VisionService.java
- ⏳ OcrService.java
- ⏳ DocumentClassifierService.java
- ⏳ DataExtractionService.java
- ⏳ ImagePreprocessingService.java
- ⏳ StorageService.java

### 7. Service Implementations (6 files)
- ⏳ VisionServiceImpl.java
- ⏳ TesseractOcrServiceImpl.java
- ⏳ DocumentClassifierServiceImpl.java
- ⏳ DataExtractionServiceImpl.java
- ⏳ ImagePreprocessingServiceImpl.java
- ⏳ MinioStorageServiceImpl.java

### 8. Controllers (2 files)
- ⏳ VisionController.java
- ⏳ TemplateController.java

### 9. Configuration Classes (7 files)
- ⏳ VisionProperties.java
- ⏳ SecurityConfig.java
- ⏳ RedisConfig.java
- ⏳ MinioConfig.java
- ⏳ KafkaConfig.java
- ⏳ AsyncConfig.java
- ⏳ OpenApiConfig.java

### 10. Exception Classes (6 files)
- ⏳ VisionException.java
- ⏳ DocumentNotFoundException.java
- ⏳ UnsupportedDocumentTypeException.java
- ⏳ OcrProcessingException.java
- ⏳ InvalidDocumentException.java
- ⏳ GlobalExceptionHandler.java

### 11. Event Classes (4 files)
- ⏳ DocumentUploadedEvent.java
- ⏳ ProcessingCompletedEvent.java
- ⏳ ProcessingFailedEvent.java
- ⏳ VisionEventPublisher.java

### 12. Utilities (4 files)
- ⏳ JwtUtil.java
- ⏳ ImageUtil.java
- ⏳ PdfUtil.java
- ⏳ DataExtractionUtil.java

### 13. Filters (1 file)
- ⏳ JwtAuthenticationFilter.java

### 14. Consumers (1 file)
- ⏳ DocumentUploadConsumer.java

### 15. Deployment Files (5 files)
- ⏳ Dockerfile
- ⏳ k8s/configmap.yaml
- ⏳ k8s/deployment.yaml
- ⏳ k8s/service.yaml
- ⏳ k8s/hpa.yaml

### 16. Documentation (2 files)
- ⏳ README.md
- ⏳ FEATURE_SUMMARY.md

### 17. Main Application (1 file)
- ⏳ VisionProcessingApplication.java

---

## KEY TECHNICAL DECISIONS

### OCR Engine: Tesseract 5.x
**Rationale**: Open-source, production-ready, supports 100+ languages
**Alternative**: Google Cloud Vision API (for enhanced accuracy)

### Image Processing: Apache PDFBox + ImageIO
**Rationale**: Mature, well-documented, handles PDF and images

### Storage: MinIO (local) / S3 (production)
**Rationale**: S3-compatible, scalable, cost-effective

### Document Types Supported:
1. **Receipts** - Extract merchant, date, total, items
2. **Invoices** - Extract vendor, invoice number, line items, total
3. **Bank Statements** - Extract transactions, balances
4. **Checks** - Extract routing number, account number, amount
5. **ID Documents** - Extract name, DOB, ID number (KYC)
6. **Generic Documents** - Full text extraction

### Processing Flow:
```
Upload → Validate → Store → Preprocess → OCR → Extract → Classify → Store Results → Publish Event
```

### Async Processing:
- Small documents (< 1MB): Synchronous
- Large documents (> 1MB): Async via Kafka

---

## API ENDPOINTS (8 endpoints)

### Document Upload & Processing
1. **POST /v1/vision/upload** - Upload document for processing
2. **GET /v1/vision/documents/{id}** - Get document metadata
3. **GET /v1/vision/documents/{id}/status** - Check processing status
4. **GET /v1/vision/documents/{id}/ocr** - Get OCR results
5. **GET /v1/vision/documents/{id}/extracted** - Get structured data
6. **GET /v1/vision/documents** - List user's documents (paginated)
7. **DELETE /v1/vision/documents/{id}** - Delete document (soft delete)

### Template Management (Admin)
8. **GET /v1/vision/templates** - List extraction templates
9. **POST /v1/vision/templates** - Create extraction template
10. **PUT /v1/vision/templates/{id}** - Update template
11. **DELETE /v1/vision/templates/{id}** - Delete template

---

## DATABASE SCHEMA

### Table: vision_documents
```sql
CREATE TABLE vision_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    storage_key VARCHAR(500) NOT NULL,
    processing_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    confidence_score DECIMAL(5,2),
    error_message TEXT,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_vision_documents_user_id ON vision_documents(user_id);
CREATE INDEX idx_vision_documents_status ON vision_documents(processing_status);
CREATE INDEX idx_vision_documents_type ON vision_documents(document_type);
CREATE INDEX idx_vision_documents_created ON vision_documents(created_at DESC);
```

### Table: ocr_results
```sql
CREATE TABLE ocr_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id UUID NOT NULL REFERENCES vision_documents(id),
    raw_text TEXT NOT NULL,
    confidence_score DECIMAL(5,2),
    language_detected VARCHAR(10),
    page_number INT NOT NULL DEFAULT 1,
    processing_time_ms BIGINT,
    ocr_engine VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_ocr_results_document_id ON ocr_results(document_id);
```

### Table: extraction_templates
```sql
CREATE TABLE extraction_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_type VARCHAR(50) NOT NULL UNIQUE,
    template_name VARCHAR(100) NOT NULL,
    extraction_rules JSONB NOT NULL,
    validation_rules JSONB,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_by UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);
```

---

## KAFKA TOPICS

### Published:
- `banking.vision.document-uploaded` - Document uploaded and queued
- `banking.vision.processing-completed` - Processing finished successfully
- `banking.vision.processing-failed` - Processing failed with error

### Consumed:
- `banking.vision.document-uploaded` - Trigger async processing

---

## REDIS CACHE KEYS

- `vision:document:{id}` - Document metadata (TTL: 1 hour)
- `vision:ocr:{id}` - OCR results (TTL: 1 hour)
- `vision:status:{id}` - Processing status (TTL: 24 hours)
- `vision:template:{type}` - Extraction templates (TTL: 24 hours)

---

## MINIO/S3 STRUCTURE

```
banking-documents/
├── originals/
│   ├── {userId}/
│   │   ├── {documentId}.{ext}
├── processed/
│   ├── {userId}/
│   │   ├── {documentId}_preprocessed.png
└── thumbnails/
    ├── {userId}/
        ├── {documentId}_thumb.jpg
```

---

## ENVIRONMENT VARIABLES

```properties
# Server
SERVER_PORT=8018

# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=banking_vision
DB_USERNAME=admin
DB_PASSWORD=admin

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# MinIO/S3
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=admin
MINIO_SECRET_KEY=password
MINIO_BUCKET=banking-documents

# Tesseract
TESSERACT_DATA_PATH=/usr/share/tesseract-ocr/4.00/tessdata
TESSERACT_LANGUAGE=eng

# Vision Processing
VISION_MAX_FILE_SIZE=10485760
VISION_SUPPORTED_FORMATS=pdf,png,jpg,jpeg,tiff
VISION_ASYNC_THRESHOLD_MB=1
VISION_OCR_TIMEOUT_SECONDS=60

# JWT
JWT_SECRET=your-256-bit-secret

# AI Enhancement (Optional)
OPENAI_API_KEY=
GPT_VISION_ENABLED=false
```

---

## SAMPLE CURL REQUESTS

### 1. Upload Receipt
```bash
curl -X POST http://localhost:8018/api/v1/vision/upload \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@receipt.jpg" \
  -F "documentType=RECEIPT"
```

**Response:**
```json
{
  "success": true,
  "data": {
    "documentId": "550e8400-e29b-41d4-a716-446655440000",
    "status": "PROCESSING",
    "estimatedCompletionTime": "2024-01-15T10:30:05Z"
  },
  "traceId": "abc123",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### 2. Check Processing Status
```bash
curl -X GET http://localhost:8018/api/v1/vision/documents/550e8400-e29b-41d4-a716-446655440000/status \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "success": true,
  "data": {
    "documentId": "550e8400-e29b-41d4-a716-446655440000",
    "status": "COMPLETED",
    "confidenceScore": 95.5,
    "processingTimeMs": 2340
  },
  "traceId": "abc124",
  "timestamp": "2024-01-15T10:30:10Z"
}
```

### 3. Get Extracted Data
```bash
curl -X GET http://localhost:8018/api/v1/vision/documents/550e8400-e29b-41d4-a716-446655440000/extracted \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "success": true,
  "data": {
    "documentType": "RECEIPT",
    "extractedData": {
      "merchant": "Starbucks Coffee",
      "date": "2024-01-15",
      "total": 15.75,
      "currency": "USD",
      "items": [
        {
          "description": "Latte",
          "quantity": 2,
          "unitPrice": 5.50,
          "total": 11.00
        },
        {
          "description": "Croissant",
          "quantity": 1,
          "unitPrice": 4.75,
          "total": 4.75
        }
      ],
      "tax": 1.42,
      "paymentMethod": "CREDIT_CARD"
    },
    "confidenceScore": 95.5,
    "rawText": "STARBUCKS COFFEE\\n123 Main St\\n..."
  },
  "traceId": "abc125",
  "timestamp": "2024-01-15T10:30:15Z"
}
```

---

## TESTING STRATEGY

### Unit Tests (Target: 80% coverage)
- OcrService: Test text extraction accuracy
- DocumentClassifier: Test document type detection
- DataExtraction: Test pattern matching
- ImagePreprocessing: Test image enhancement

### Integration Tests
- End-to-end document upload and processing
- MinIO storage operations
- Kafka event publishing
- Redis caching

### Performance Tests
- OCR processing time (target: < 5s for single page)
- Concurrent uploads (target: 100 req/s)
- Large document handling (target: 10MB in < 30s)

---

## DEPLOYMENT CHECKLIST

### Prerequisites
1. ✅ Tesseract OCR installed on container
2. ✅ Tessdata language files available
3. ✅ MinIO/S3 bucket created
4. ✅ PostgreSQL database created
5. ✅ Redis available
6. ✅ Kafka topics created

### Docker Build
```bash
docker build -t vision-processing-service:1.0.0 .
```

### Kubernetes Deploy
```bash
kubectl apply -f k8s/
```

---

## KNOWN LIMITATIONS

1. **OCR Accuracy**: 85-95% depending on image quality
2. **Handwriting**: Limited support (printed text only)
3. **Complex Layouts**: May struggle with multi-column documents
4. **Language Support**: English primary, others require tessdata
5. **Processing Time**: 2-10 seconds per page depending on quality

---

## FUTURE ENHANCEMENTS

1. **GPT-4 Vision Integration**: Enhanced accuracy for complex documents
2. **Handwriting Recognition**: Support for handwritten receipts
3. **Multi-language**: Automatic language detection and processing
4. **Template Learning**: ML-based template generation
5. **Batch Processing**: Process multiple documents in one request
6. **Real-time Processing**: WebSocket for live OCR feedback
7. **Mobile SDK**: Native mobile OCR processing

---

## NEXT STEPS TO COMPLETE IMPLEMENTATION

1. Create all 60+ source files listed above
2. Implement Tesseract OCR integration
3. Build image preprocessing pipeline
4. Create extraction templates for each document type
5. Implement MinIO storage operations
6. Add comprehensive tests
7. Create deployment manifests
8. Write complete documentation

---

**Estimated Implementation Time**: 40-50 hours  
**Complexity**: HIGH (OCR integration, image processing, async workflows)  
**Priority**: MEDIUM (enables transaction categorization and expense tracking)

---

**Status**: Blueprint Complete - Ready for Full Implementation  
**Next**: Create all source files and complete Feature 18
