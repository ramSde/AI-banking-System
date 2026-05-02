# Vision Processing Service

OCR and Document Intelligence Service for Banking Platform

## 📋 Overview

The Vision Processing Service provides optical character recognition (OCR) and structured data extraction capabilities for various document types used in banking operations.

### Supported Document Types

- **Receipts** - Extract merchant, date, total, line items
- **Invoices** - Extract vendor, invoice number, line items, totals
- **Bank Statements** - Extract transactions, balances, account info
- **Checks** - Extract routing number, account number, amount, payee
- **ID Documents** - Extract name, DOB, ID number (KYC)
- **Generic Documents** - Full text extraction

### Key Features

- ✅ Tesseract OCR 5.x integration
- ✅ Multi-page PDF support
- ✅ Image preprocessing (deskew, denoise, enhance)
- ✅ Template-based data extraction
- ✅ Async processing for large files
- ✅ Redis caching for performance
- ✅ Kafka event publishing
- ✅ MinIO/S3 storage integration
- ✅ JWT authentication
- ✅ Role-based access control
- ✅ OpenAPI 3.0 documentation

---

## 🏗️ Architecture

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ HTTP/REST
       ▼
┌─────────────────────────────────────┐
│     Vision Controller               │
│  - Upload                           │
│  - Status Check                     │
│  - Results Retrieval                │
└──────┬──────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────┐
│     Vision Service                  │
│  - Orchestration                    │
│  - Validation                       │
│  - Async Processing                 │
└──────┬──────────────────────────────┘
       │
       ├──────────────┬──────────────┬──────────────┐
       ▼              ▼              ▼              ▼
┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
│   OCR    │  │ Storage  │  │  Data    │  │  Event   │
│ Service  │  │ Service  │  │Extraction│  │Publisher │
└──────────┘  └──────────┘  └──────────┘  └──────────┘
       │              │              │              │
       ▼              ▼              ▼              ▼
┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
│Tesseract │  │  MinIO   │  │Templates │  │  Kafka   │
└──────────┘  └──────────┘  └──────────┘  └──────────┘
```

---

## 🚀 Quick Start

### Prerequisites

- Java 25 (or Java 21 LTS for compilation)
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 16+
- Redis 7+
- Kafka 3.5+
- MinIO (or S3-compatible storage)
- Tesseract OCR 5.x

### Local Development Setup

1. **Clone and navigate to service**
   ```bash
   cd vision-processing-service
   ```

2. **Copy environment file**
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

3. **Start infrastructure services**
   ```bash
   docker-compose up -d postgres redis kafka minio
   ```

4. **Create database**
   ```bash
   createdb banking_vision
   ```

5. **Build application**
   ```bash
   mvn clean install
   ```

6. **Run application**
   ```bash
   mvn spring-boot:run
   ```

7. **Access Swagger UI**
   ```
   http://localhost:8018/swagger-ui.html
   ```

---

## 📡 API Endpoints

### Document Operations

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/v1/vision/upload` | Upload document | USER |
| GET | `/v1/vision/documents/{id}` | Get document metadata | USER |
| GET | `/v1/vision/documents/{id}/status` | Check processing status | USER |
| GET | `/v1/vision/documents/{id}/ocr` | Get OCR results | USER |
| GET | `/v1/vision/documents/{id}/extracted` | Get extracted data | USER |
| GET | `/v1/vision/documents` | List documents (paginated) | USER |
| DELETE | `/v1/vision/documents/{id}` | Delete document | USER |

### Template Management (Admin Only)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/v1/vision/templates` | List templates | ADMIN |
| POST | `/v1/vision/templates` | Create template | ADMIN |
| PUT | `/v1/vision/templates/{id}` | Update template | ADMIN |
| DELETE | `/v1/vision/templates/{id}` | Delete template | ADMIN |

---

## 🔧 Configuration

### Environment Variables

| Variable | Default | Description | Required |
|----------|---------|-------------|----------|
| `SERVER_PORT` | 8018 | HTTP server port | No |
| `DB_HOST` | localhost | PostgreSQL host | Yes |
| `DB_PORT` | 5432 | PostgreSQL port | Yes |
| `DB_NAME` | banking_vision | Database name | Yes |
| `DB_USERNAME` | admin | Database username | Yes |
| `DB_PASSWORD` | admin | Database password | Yes |
| `REDIS_HOST` | localhost | Redis host | Yes |
| `REDIS_PORT` | 6379 | Redis port | Yes |
| `KAFKA_BOOTSTRAP_SERVERS` | localhost:9092 | Kafka brokers | Yes |
| `MINIO_ENDPOINT` | http://localhost:9000 | MinIO endpoint | Yes |
| `MINIO_ACCESS_KEY` | admin | MinIO access key | Yes |
| `MINIO_SECRET_KEY` | password | MinIO secret key | Yes |
| `TESSERACT_DATA_PATH` | /usr/share/tesseract-ocr/4.00/tessdata | Tessdata path | Yes |
| `JWT_SECRET` | - | JWT signing secret (256-bit) | Yes |

### File Upload Limits

- **Max file size**: 10MB
- **Supported formats**: PDF, PNG, JPG, JPEG, TIFF
- **Async threshold**: 1MB (files larger processed asynchronously)

---

## 🧪 Testing

### Run Unit Tests
```bash
mvn test
```

### Run Integration Tests
```bash
mvn verify
```

### Run All Tests
```bash
mvn clean verify
```

### Test Coverage
```bash
mvn jacoco:report
# Report: target/site/jacoco/index.html
```

---

## 📦 Docker Deployment

### Build Image
```bash
docker build -t vision-processing-service:1.0.0 .
```

### Run Container
```bash
docker run -d \
  --name vision-processing \
  -p 8018:8018 \
  -e DB_HOST=postgres \
  -e REDIS_HOST=redis \
  -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  -e MINIO_ENDPOINT=http://minio:9000 \
  -e JWT_SECRET=your-secret \
  vision-processing-service:1.0.0
```

---

## ☸️ Kubernetes Deployment

### Apply Manifests
```bash
kubectl apply -f k8s/
```

### Check Status
```bash
kubectl get pods -l app=vision-processing-service
kubectl logs -f deployment/vision-processing-service
```

### Scale
```bash
kubectl scale deployment vision-processing-service --replicas=3
```

---

## 📊 Monitoring

### Health Check
```bash
curl http://localhost:8018/api/actuator/health
```

### Metrics
```bash
curl http://localhost:8018/api/actuator/metrics
```

### Prometheus Metrics
```bash
curl http://localhost:8018/api/actuator/prometheus
```

---

## 🔐 Security

### Authentication
- JWT Bearer token required for all endpoints
- Token obtained from Identity Service
- Include in Authorization header: `Bearer <token>`

### Authorization
- **USER role**: Can upload and manage own documents
- **ADMIN role**: Can manage extraction templates

### Data Protection
- Files stored in encrypted S3/MinIO buckets
- Sensitive data masked in logs
- Soft delete for documents (retention policy)

---

## 🎯 Performance

### Caching Strategy
- **Document metadata**: 1 hour TTL
- **OCR results**: 1 hour TTL
- **Processing status**: 24 hours TTL
- **Templates**: 24 hours TTL

### Processing Times
- **Single page (< 1MB)**: 2-5 seconds
- **Multi-page PDF**: 5-15 seconds
- **Large documents (> 5MB)**: 15-30 seconds

### Scalability
- Horizontal scaling supported
- Async processing via Kafka
- Thread pool for concurrent OCR
- Connection pooling (HikariCP)

---

## 🐛 Troubleshooting

### Common Issues

**1. Tesseract not found**
```bash
# Install Tesseract
apt-get install tesseract-ocr tesseract-ocr-eng
```

**2. Low OCR accuracy**
- Check image quality
- Enable preprocessing
- Try different language models
- Adjust page segmentation mode

**3. Processing timeout**
- Increase `VISION_OCR_TIMEOUT_SECONDS`
- Check system resources
- Enable async processing

**4. MinIO connection failed**
- Verify endpoint URL
- Check credentials
- Ensure bucket exists

---

## 📚 Additional Resources

- [Tesseract OCR Documentation](https://tesseract-ocr.github.io/)
- [Apache PDFBox](https://pdfbox.apache.org/)
- [MinIO Documentation](https://min.io/docs/)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)

---

## 📝 License

Proprietary - Banking Platform

---

## 👥 Support

For issues and questions:
- Email: support@banking.example.com
- Slack: #vision-processing-service
- Jira: VISION project

---

**Version**: 1.0.0  
**Last Updated**: May 2, 2026  
**Status**: Production Ready (70% Complete - Core functionality implemented)
