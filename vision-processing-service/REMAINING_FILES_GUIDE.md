# Vision Processing Service - Remaining Files Implementation Guide

## Status: 50% COMPLETE (30/60+ files created)

This document provides implementation guidance for all remaining files.

---

## ✅ COMPLETED FILES (30)

### Core & Domain (10 files)
- ✅ VisionProcessingApplication.java
- ✅ DocumentType.java
- ✅ ProcessingStatus.java
- ✅ ConfidenceLevel.java
- ✅ VisionDocument.java
- ✅ OcrResult.java
- ✅ ExtractionTemplate.java
- ✅ VisionDocumentRepository.java
- ✅ OcrResultRepository.java
- ✅ ExtractionTemplateRepository.java

### DTOs (9 files)
- ✅ ApiResponse.java
- ✅ DocumentUploadRequest.java
- ✅ DocumentUploadResponse.java
- ✅ ProcessingStatusResponse.java
- ✅ OcrResultResponse.java
- ✅ ExtractedDataResponse.java
- ✅ ReceiptData.java
- ✅ InvoiceData.java
- ✅ CheckData.java

### Configuration (7 files)
- ✅ VisionProperties.java
- ✅ SecurityConfig.java
- ✅ RedisConfig.java
- ✅ MinioConfig.java
- ✅ KafkaConfig.java
- ✅ AsyncConfig.java
- ✅ OpenApiConfig.java

### Services (7 files)
- ✅ VisionService.java (interface)
- ✅ OcrService.java (interface)
- ✅ DocumentClassifierService.java (interface)
- ✅ DataExtractionService.java (interface)
- ✅ ImagePreprocessingService.java (interface)
- ✅ StorageService.java (interface)
- ✅ VisionServiceImpl.java (implementation)

### Events & Utilities (7 files)
- ✅ DocumentUploadedEvent.java
- ✅ ProcessingCompletedEvent.java
- ✅ ProcessingFailedEvent.java
- ✅ VisionEventPublisher.java
- ✅ JwtUtil.java
- ✅ JwtAuthenticationFilter.java
- ✅ GlobalExceptionHandler.java

---

## ⏳ REMAINING FILES (30+)

### Service Implementations (5 files) - HIGH PRIORITY

#### 1. TesseractOcrServiceImpl.java
```java
package com.banking.vision.service.impl;

@Slf4j
@Service
@RequiredArgsConstructor
public class TesseractOcrServiceImpl implements OcrService {
    
    private final VisionProperties visionProperties;
    private final OcrResultRepository ocrResultRepository;
    private final ImagePreprocessingService preprocessingService;
    
    @Override
    public OcrResult performOcr(File imageFile, UUID documentId, int pageNumber) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Initialize Tesseract
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(visionProperties.getOcr().getTessdataPath());
            tesseract.setLanguage(visionProperties.getOcr().getDefaultLanguage());
            tesseract.setPageSegMode(visionProperties.getOcr().getPageSegmentationMode());
            tesseract.setOcrEngineMode(visionProperties.getOcr().getEngineMode());
            
            // Preprocess image
            BufferedImage preprocessed = preprocessingService.preprocessImage(imageFile);
            
            // Perform OCR
            String text = tesseract.doOCR(preprocessed);
            
            // Get confidence
            double confidence = calculateConfidence(tesseract, preprocessed);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Create and save result
            OcrResult result = OcrResult.builder()
                .documentId(documentId)
                .rawText(text)
                .confidenceScore(confidence)
                .pageNumber(pageNumber)
                .processingTimeMs(processingTime)
                .ocrEngine("Tesseract")
                .build();
            
            return ocrResultRepository.save(result);
            
        } catch (TesseractException e) {
            throw new OcrProcessingException("OCR failed: " + e.getMessage(), e);
        }
    }
    
    // Implement other methods...
}
```

#### 2. MinioStorageServiceImpl.java
```java
package com.banking.vision.service.impl;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageServiceImpl implements StorageService {
    
    private final MinioClient minioClient;
    private final VisionProperties visionProperties;
    
    @PostConstruct
    public void init() {
        // Ensure bucket exists
        try {
            boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(visionProperties.getStorage().getBucketName())
                    .build()
            );
            
            if (!exists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(visionProperties.getStorage().getBucketName())
                        .build()
                );
            }
        } catch (Exception e) {
            log.error("Failed to initialize MinIO bucket", e);
        }
    }
    
    @Override
    public String uploadFile(InputStream inputStream, String fileName, 
                            String contentType, UUID userId, UUID documentId) {
        String storageKey = buildOriginalStorageKey(userId, documentId, 
            getFileExtension(fileName));
        
        try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(visionProperties.getStorage().getBucketName())
                    .object(storageKey)
                    .stream(inputStream, -1, 10485760) // 10MB part size
                    .contentType(contentType)
                    .build()
            );
            
            return storageKey;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }
    
    // Implement other methods...
}
```

#### 3. DocumentClassifierServiceImpl.java
```java
package com.banking.vision.service.impl;

@Slf4j
@Service
public class DocumentClassifierServiceImpl implements DocumentClassifierService {
    
    private static final Map<DocumentType, List<String>> KEYWORDS = Map.of(
        DocumentType.RECEIPT, List.of("receipt", "total", "subtotal", "tax", "merchant"),
        DocumentType.INVOICE, List.of("invoice", "bill to", "due date", "invoice number"),
        DocumentType.CHECK, List.of("pay to the order of", "routing", "account", "check"),
        DocumentType.BANK_STATEMENT, List.of("statement", "balance", "transaction", "account summary"),
        DocumentType.ID_DOCUMENT, List.of("driver license", "passport", "identification", "date of birth")
    );
    
    @Override
    public DocumentType classifyDocument(String ocrText) {
        String lowerText = ocrText.toLowerCase();
        
        Map<DocumentType, Integer> scores = new HashMap<>();
        
        for (Map.Entry<DocumentType, List<String>> entry : KEYWORDS.entrySet()) {
            int score = 0;
            for (String keyword : entry.getValue()) {
                if (lowerText.contains(keyword)) {
                    score++;
                }
            }
            scores.put(entry.getKey(), score);
        }
        
        return scores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(DocumentType.GENERIC);
    }
    
    // Implement other methods...
}
```

#### 4. DataExtractionServiceImpl.java
```java
package com.banking.vision.service.impl;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataExtractionServiceImpl implements DataExtractionService {
    
    private final ExtractionTemplateRepository templateRepository;
    
    @Override
    public Map<String, Object> extractData(String ocrText, DocumentType documentType) {
        return switch (documentType) {
            case RECEIPT -> extractReceiptData(ocrText);
            case INVOICE -> extractInvoiceData(ocrText);
            case CHECK -> extractCheckData(ocrText);
            case BANK_STATEMENT -> extractBankStatementData(ocrText);
            case ID_DOCUMENT -> extractIdDocumentData(ocrText);
            default -> Map.of("rawText", ocrText);
        };
    }
    
    @Override
    public Map<String, Object> extractReceiptData(String ocrText) {
        Map<String, Object> data = new HashMap<>();
        
        // Extract merchant (usually first line)
        String[] lines = ocrText.split("\\n");
        if (lines.length > 0) {
            data.put("merchant", lines[0].trim());
        }
        
        // Extract total using regex
        Pattern totalPattern = Pattern.compile("total[:\\s]*\\$?([0-9]+\\.[0-9]{2})", 
            Pattern.CASE_INSENSITIVE);
        Matcher totalMatcher = totalPattern.matcher(ocrText);
        if (totalMatcher.find()) {
            data.put("total", new BigDecimal(totalMatcher.group(1)));
        }
        
        // Extract date
        Pattern datePattern = Pattern.compile("(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})");
        Matcher dateMatcher = datePattern.matcher(ocrText);
        if (dateMatcher.find()) {
            data.put("date", dateMatcher.group(1));
        }
        
        return data;
    }
    
    // Implement other extraction methods...
}
```

#### 5. ImagePreprocessingServiceImpl.java
```java
package com.banking.vision.service.impl;

@Slf4j
@Service
public class ImagePreprocessingServiceImpl implements ImagePreprocessingService {
    
    @Override
    public BufferedImage preprocessImage(BufferedImage image) {
        // Apply preprocessing pipeline
        BufferedImage processed = image;
        
        // 1. Convert to grayscale
        processed = convertToGrayscale(processed);
        
        // 2. Enhance contrast
        processed = enhanceContrast(processed);
        
        // 3. Denoise
        processed = denoise(processed);
        
        // 4. Binarize
        processed = binarize(processed);
        
        return processed;
    }
    
    @Override
    public BufferedImage convertToGrayscale(BufferedImage image) {
        BufferedImage grayscale = new BufferedImage(
            image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY
        );
        
        Graphics2D g = grayscale.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        
        return grayscale;
    }
    
    // Implement other preprocessing methods...
}
```

### Controllers (2 files) - HIGH PRIORITY

#### 6. VisionController.java
```java
package com.banking.vision.controller;

@Slf4j
@RestController
@RequestMapping("/api/v1/vision")
@RequiredArgsConstructor
@Tag(name = "Vision Processing", description = "Document OCR and vision processing endpoints")
public class VisionController {
    
    private final VisionService visionService;
    
    @PostMapping("/upload")
    @Operation(summary = "Upload document for processing")
    public ResponseEntity<ApiResponse<DocumentUploadResponse>> uploadDocument(
        @RequestParam("file") MultipartFile file,
        @Valid @ModelAttribute DocumentUploadRequest request,
        @AuthenticationPrincipal UUID userId
    ) {
        VisionDocument document = visionService.uploadDocument(
            file, request.getDocumentType(), userId, null
        );
        
        DocumentUploadResponse response = DocumentUploadResponse.builder()
            .documentId(document.getId())
            .documentType(document.getDocumentType())
            .filename(document.getOriginalFilename())
            .fileSize(document.getFileSize())
            .status(document.getProcessingStatus())
            .uploadedAt(document.getCreatedAt())
            .build();
        
        return ResponseEntity.ok(ApiResponse.success(response, UUID.randomUUID().toString()));
    }
    
    @GetMapping("/documents/{id}")
    @Operation(summary = "Get document by ID")
    public ResponseEntity<ApiResponse<VisionDocument>> getDocument(
        @PathVariable UUID id,
        @AuthenticationPrincipal UUID userId
    ) {
        VisionDocument document = visionService.getDocument(id, userId);
        return ResponseEntity.ok(ApiResponse.success(document, UUID.randomUUID().toString()));
    }
    
    // Implement other endpoints...
}
```

#### 7. TemplateController.java
```java
package com.banking.vision.controller;

@Slf4j
@RestController
@RequestMapping("/api/v1/vision/templates")
@RequiredArgsConstructor
@Tag(name = "Extraction Templates", description = "Template management endpoints (Admin only)")
public class TemplateController {
    
    private final ExtractionTemplateRepository templateRepository;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all extraction templates")
    public ResponseEntity<ApiResponse<List<ExtractionTemplate>>> listTemplates() {
        List<ExtractionTemplate> templates = templateRepository.findAllActive();
        return ResponseEntity.ok(ApiResponse.success(templates, UUID.randomUUID().toString()));
    }
    
    // Implement other template management endpoints...
}
```

### Utilities (3 files) - MEDIUM PRIORITY

#### 8. ImageUtil.java
- Image format conversion
- Thumbnail generation
- Image validation
- Format detection

#### 9. PdfUtil.java
- PDF to image conversion
- Page extraction
- PDF validation
- Metadata extraction

#### 10. DataExtractionUtil.java
- Regex pattern helpers
- Date parsing
- Currency parsing
- Field validation

### Kafka Consumer (1 file) - MEDIUM PRIORITY

#### 11. DocumentUploadConsumer.java
```java
package com.banking.vision.consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentUploadConsumer {
    
    private final VisionService visionService;
    
    @KafkaListener(
        topics = "banking.vision.document-uploaded",
        groupId = "vision-processing-consumer-group"
    )
    public void handleDocumentUploaded(
        @Payload DocumentUploadedEvent event,
        Acknowledgment acknowledgment
    ) {
        try {
            log.info("Received DocumentUploadedEvent: {}", event.getEventId());
            
            // Trigger async processing
            visionService.processDocument(event.getPayload().getDocumentId());
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Failed to process DocumentUploadedEvent", e);
            // Don't acknowledge - will retry
        }
    }
}
```

### Configuration Files (7 files) - HIGH PRIORITY

#### 12. application.yml
```yaml
spring:
  application:
    name: vision-processing-service
  
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:banking_vision}
    username: ${DB_USERNAME:admin}
    password: ${DB_PASSWORD:admin}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  
  liquibase:
    change-log: classpath:db/changelog/changelog-master.xml
    enabled: true
  
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: 0
  
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      group-id: vision-processing-consumer-group
  
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 15MB

server:
  port: ${SERVER_PORT:8018}
  servlet:
    context-path: /api
  compression:
    enabled: true
    min-response-size: 1024

vision:
  ocr:
    tessdata-path: ${TESSERACT_DATA_PATH:/usr/share/tesseract-ocr/4.00/tessdata}
    default-language: ${TESSERACT_LANGUAGE:eng}
    timeout-seconds: 60
  storage:
    endpoint: ${MINIO_ENDPOINT:http://localhost:9000}
    access-key: ${MINIO_ACCESS_KEY:admin}
    secret-key: ${MINIO_SECRET_KEY:password}
    bucket-name: ${MINIO_BUCKET:banking-documents}
  processing:
    async-threshold-bytes: 1048576
    max-concurrent-tasks: 10
    enable-preprocessing: true
  upload:
    max-file-size-bytes: 10485760

jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-change-in-production}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

#### 13-15. Profile-specific configs (application-dev.yml, application-staging.yml, application-prod.yml)
- Override environment-specific values
- Different log levels
- Different connection pools
- Different cache TTLs

#### 16. logback-spring.xml
- JSON logging format
- Include traceId, spanId, userId
- Different appenders for dev/prod
- Log rotation policies

#### 17. .env.example
- Document all environment variables
- Provide example values
- Include descriptions

#### 18. liquibase.properties
- Liquibase configuration
- Database connection details

### Database Migrations (5 files) - HIGH PRIORITY

#### 19. changelog-master.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
    http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">

    <include file="db/changelog/V001__create_vision_documents.sql"/>
    <include file="db/changelog/V002__create_ocr_results.sql"/>
    <include file="db/changelog/V003__create_extraction_templates.sql"/>
    <include file="db/changelog/V004__create_indexes.sql"/>
    <include file="db/changelog/V005__seed_templates.sql"/>
</databaseChangeLog>
```

#### 20-24. SQL migration files
- V001: Create vision_documents table
- V002: Create ocr_results table
- V003: Create extraction_templates table
- V004: Create all indexes
- V005: Seed default templates

### Deployment Files (5 files) - MEDIUM PRIORITY

#### 25. Dockerfile
```dockerfile
FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:25-jre-alpine
RUN apk add --no-cache tesseract-ocr tesseract-ocr-data-eng
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
RUN addgroup -g 1000 appuser && adduser -D -u 1000 -G appuser appuser
USER appuser
EXPOSE 8018
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8018/api/actuator/health || exit 1
ENTRYPOINT ["java", "-Xms512m", "-Xmx1g", "-XX:+UseG1GC", "-jar", "app.jar"]
```

#### 26-29. Kubernetes manifests
- ConfigMap: Non-sensitive configuration
- Deployment: 2 replicas, resource limits, probes
- Service: ClusterIP
- HPA: CPU-based autoscaling

### Documentation (2 files) - LOW PRIORITY

#### 30. README.md
- Service overview
- Prerequisites
- Local setup
- API documentation
- Environment variables
- Testing instructions

#### 31. FEATURE_SUMMARY.md
- Feature completion summary
- Architecture decisions
- Known limitations
- Future enhancements

---

## 🎯 IMPLEMENTATION PRIORITY

### Phase 1: Core Functionality (Files 1-7, 12-24)
**Goal**: Make service runnable and testable
- Service implementations
- Controllers
- Configuration files
- Database migrations

### Phase 2: Supporting Features (Files 8-11)
**Goal**: Complete business logic
- Utility classes
- Kafka consumer
- Event handling

### Phase 3: Deployment (Files 25-29)
**Goal**: Make service deployable
- Dockerfile
- Kubernetes manifests

### Phase 4: Documentation (Files 30-31)
**Goal**: Complete documentation
- README
- Feature summary

---

## 📝 NOTES

1. **Tesseract Installation**: Dockerfile includes Tesseract OCR installation
2. **Image Processing**: Uses Java AWT/ImageIO for preprocessing
3. **PDF Handling**: Apache PDFBox for PDF to image conversion
4. **Storage**: MinIO client for S3-compatible storage
5. **Async Processing**: Spring @Async for large file processing
6. **Caching**: Redis for document metadata and OCR results
7. **Events**: Kafka for async communication

---

## ✅ NEXT STEPS

1. Create remaining service implementations (5 files)
2. Create controllers (2 files)
3. Create configuration files (7 files)
4. Create database migrations (5 files)
5. Create utilities (3 files)
6. Create Kafka consumer (1 file)
7. Create deployment files (5 files)
8. Create documentation (2 files)

**Total Remaining**: 30 files
**Estimated Time**: 2-3 hours

