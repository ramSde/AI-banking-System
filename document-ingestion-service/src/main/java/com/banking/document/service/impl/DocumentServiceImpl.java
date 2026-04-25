package com.banking.document.service.impl;

import com.banking.document.domain.Document;
import com.banking.document.domain.DocumentChunk;
import com.banking.document.domain.DocumentType;
import com.banking.document.domain.ProcessingStatus;
import com.banking.document.dto.DocumentResponse;
import com.banking.document.dto.DocumentSearchRequest;
import com.banking.document.dto.DocumentStatsResponse;
import com.banking.document.event.DocumentProcessedEvent;
import com.banking.document.event.DocumentProcessingFailedEvent;
import com.banking.document.exception.DocumentNotFoundException;
import com.banking.document.exception.DocumentProcessingException;
import com.banking.document.exception.UnsupportedDocumentTypeException;
import com.banking.document.mapper.DocumentMapper;
import com.banking.document.repository.DocumentChunkRepository;
import com.banking.document.repository.DocumentRepository;
import com.banking.document.service.*;
import com.banking.document.util.FileValidator;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final DocumentMapper documentMapper;
    private final MinioClient minioClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final List<TextExtractionService> textExtractionServices;
    private final ChunkingService chunkingService;
    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;
    private final FileValidator fileValidator;
    
    private final String minioBucketName;
    private final int chunkSize;
    private final int chunkOverlap;
    private final String topicProcessed;
    private final String topicProcessingFailed;

    public DocumentServiceImpl(
            DocumentRepository documentRepository,
            DocumentChunkRepository documentChunkRepository,
            DocumentMapper documentMapper,
            MinioClient minioClient,
            KafkaTemplate<String, Object> kafkaTemplate,
            List<TextExtractionService> textExtractionServices,
            ChunkingService chunkingService,
            EmbeddingService embeddingService,
            VectorStoreService vectorStoreService,
            FileValidator fileValidator,
            @Value("${minio.bucket-name}") String minioBucketName,
            @Value("${document.processing.chunk-size}") int chunkSize,
            @Value("${document.processing.chunk-overlap}") int chunkOverlap,
            @Value("${document.kafka.topics.processed}") String topicProcessed,
            @Value("${document.kafka.topics.processing-failed}") String topicProcessingFailed) {
        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.documentMapper = documentMapper;
        this.minioClient = minioClient;
        this.kafkaTemplate = kafkaTemplate;
        this.textExtractionServices = textExtractionServices;
        this.chunkingService = chunkingService;
        this.embeddingService = embeddingService;
        this.vectorStoreService = vectorStoreService;
        this.fileValidator = fileValidator;
        this.minioBucketName = minioBucketName;
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
        this.topicProcessed = topicProcessed;
        this.topicProcessingFailed = topicProcessingFailed;
    }

    @Override
    @Transactional
    public DocumentResponse uploadDocument(MultipartFile file, DocumentType documentType, UUID userId) {
        log.info("Uploading document - filename: {}, type: {}, userId: {}", 
                file.getOriginalFilename(), documentType, userId);

        fileValidator.validateFile(file);

        String storageKey = generateStorageKey(userId, file.getOriginalFilename());
        
        try {
            uploadToMinIO(file, storageKey);
        } catch (Exception e) {
            log.error("Failed to upload file to MinIO: {}", e.getMessage(), e);
            throw new DocumentProcessingException("Failed to upload document to storage", e);
        }

        Document document = Document.builder()
                .userId(userId)
                .originalFilename(file.getOriginalFilename())
                .storageKey(storageKey)
                .mimeType(file.getContentType())
                .fileSizeBytes(file.getSize())
                .documentType(documentType)
                .processingStatus(ProcessingStatus.PENDING)
                .build();

        document = documentRepository.save(document);
        log.info("Document saved with ID: {}", document.getId());

        return documentMapper.toResponse(document);
    }

    @Override
    @Async("taskExecutor")
    @Transactional
    public void processDocument(UUID documentId) {
        log.info("Processing document: {}", documentId);

        Document document = documentRepository.findByIdAndNotDeleted(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));

        try {
            document.setProcessingStatus(ProcessingStatus.PROCESSING);
            documentRepository.save(document);

            String extractedText = extractTextFromDocument(document);
            document.setExtractedText(extractedText);

            List<String> chunks = chunkingService.chunkText(extractedText, chunkSize, chunkOverlap);
            log.info("Document {} chunked into {} chunks", documentId, chunks.size());

            List<DocumentChunk> documentChunks = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                String chunkText = chunks.get(i);
                int tokenCount = chunkingService.estimateTokenCount(chunkText);

                List<Double> embedding = embeddingService.generateEmbedding(chunkText);

                Map<String, Object> metadata = new HashMap<>();
                metadata.put("document_id", documentId.toString());
                metadata.put("user_id", document.getUserId().toString());
                metadata.put("chunk_index", i);
                metadata.put("document_type", document.getDocumentType().name());

                String vectorId = vectorStoreService.storeVector(chunkText, embedding, metadata);

                DocumentChunk chunk = DocumentChunk.builder()
                        .documentId(documentId)
                        .chunkIndex(i)
                        .chunkText(chunkText)
                        .tokenCount(tokenCount)
                        .vectorId(vectorId)
                        .metadata(metadata)
                        .build();

                documentChunks.add(chunk);
            }

            documentChunkRepository.saveAll(documentChunks);

            document.setTotalChunks(chunks.size());
            document.setProcessingStatus(ProcessingStatus.COMPLETED);
            documentRepository.save(document);

            publishDocumentProcessedEvent(document);
            log.info("Document {} processed successfully", documentId);

        } catch (Exception e) {
            log.error("Error processing document {}: {}", documentId, e.getMessage(), e);
            
            document.setProcessingStatus(ProcessingStatus.FAILED);
            document.setErrorMessage(e.getMessage());
            documentRepository.save(document);

            publishDocumentProcessingFailedEvent(document, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse getDocument(UUID documentId, UUID userId) {
        Document document = documentRepository.findByIdAndNotDeleted(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));

        if (!document.getUserId().equals(userId)) {
            throw new DocumentNotFoundException(documentId);
        }

        return documentMapper.toResponse(document);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentResponse> getUserDocuments(UUID userId, Pageable pageable) {
        Page<Document> documents = documentRepository.findByUserIdAndNotDeleted(userId, pageable);
        return documents.map(documentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentResponse> getUserDocumentsByStatus(UUID userId, ProcessingStatus status, Pageable pageable) {
        Page<Document> documents = documentRepository.findByUserIdAndStatusAndNotDeleted(userId, status, pageable);
        return documents.map(documentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentResponse> getUserDocumentsByType(UUID userId, DocumentType documentType, Pageable pageable) {
        Page<Document> documents = documentRepository.findByUserIdAndDocumentTypeAndNotDeleted(userId, documentType, pageable);
        return documents.map(documentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentResponse> getUserDocumentsByDateRange(UUID userId, Instant startDate, Instant endDate, Pageable pageable) {
        Page<Document> documents = documentRepository.findByUserIdAndCreatedAtBetweenAndNotDeleted(
                userId, startDate, endDate, pageable);
        return documents.map(documentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> searchDocuments(DocumentSearchRequest request, UUID userId) {
        log.info("Searching documents for user {} with query: {}", userId, request.getQuery());

        List<VectorStoreService.VectorSearchResult> searchResults = vectorStoreService.searchSimilar(
                request.getQuery(),
                request.getTopK(),
                request.getSimilarityThreshold()
        );

        Set<UUID> documentIds = searchResults.stream()
                .map(result -> UUID.fromString((String) result.metadata().get("document_id")))
                .collect(Collectors.toSet());

        List<Document> documents = documentIds.stream()
                .map(id -> documentRepository.findByIdAndNotDeleted(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(doc -> doc.getUserId().equals(userId))
                .collect(Collectors.toList());

        return documents.stream()
                .map(documentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentStatsResponse getUserDocumentStats(UUID userId) {
        Long totalDocuments = documentRepository.countByUserIdAndNotDeleted(userId);
        Long pendingDocuments = documentRepository.countByUserIdAndStatusAndNotDeleted(userId, ProcessingStatus.PENDING);
        Long processingDocuments = documentRepository.countByUserIdAndStatusAndNotDeleted(userId, ProcessingStatus.PROCESSING);
        Long completedDocuments = documentRepository.countByUserIdAndStatusAndNotDeleted(userId, ProcessingStatus.COMPLETED);
        Long failedDocuments = documentRepository.countByUserIdAndStatusAndNotDeleted(userId, ProcessingStatus.FAILED);

        List<Document> userDocuments = documentRepository.findByUserIdAndNotDeleted(userId, Pageable.unpaged()).getContent();
        
        Long totalChunks = userDocuments.stream()
                .mapToLong(doc -> doc.getTotalChunks() != null ? doc.getTotalChunks() : 0)
                .sum();

        Long totalSizeBytes = userDocuments.stream()
                .mapToLong(Document::getFileSizeBytes)
                .sum();

        Map<String, Long> documentsByType = userDocuments.stream()
                .collect(Collectors.groupingBy(
                        doc -> doc.getDocumentType().name(),
                        Collectors.counting()
                ));

        return DocumentStatsResponse.builder()
                .totalDocuments(totalDocuments)
                .pendingDocuments(pendingDocuments)
                .processingDocuments(processingDocuments)
                .completedDocuments(completedDocuments)
                .failedDocuments(failedDocuments)
                .totalChunks(totalChunks)
                .totalSizeBytes(totalSizeBytes)
                .documentsByType(documentsByType)
                .build();
    }

    @Override
    @CircuitBreaker(name = "minioService", fallbackMethod = "getDocumentDownloadUrlFallback")
    @Retry(name = "minioService")
    public String getDocumentDownloadUrl(UUID documentId, UUID userId) {
        Document document = documentRepository.findByIdAndNotDeleted(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));

        if (!document.getUserId().equals(userId)) {
            throw new DocumentNotFoundException(documentId);
        }

        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioBucketName)
                            .object(document.getStorageKey())
                            .expiry(1, TimeUnit.HOURS)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error generating pre-signed URL for document {}: {}", documentId, e.getMessage(), e);
            throw new DocumentProcessingException("Failed to generate download URL", e);
        }
    }

    @Override
    @Transactional
    public void deleteDocument(UUID documentId, UUID userId) {
        Document document = documentRepository.findByIdAndNotDeleted(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));

        if (!document.getUserId().equals(userId)) {
            throw new DocumentNotFoundException(documentId);
        }

        document.setDeletedAt(Instant.now());
        documentRepository.save(document);

        List<DocumentChunk> chunks = documentChunkRepository.findByDocumentIdAndNotDeleted(documentId);
        chunks.forEach(chunk -> {
            chunk.setDeletedAt(Instant.now());
            if (chunk.getVectorId() != null) {
                try {
                    vectorStoreService.deleteVector(chunk.getVectorId());
                } catch (Exception e) {
                    log.error("Error deleting vector {}: {}", chunk.getVectorId(), e.getMessage());
                }
            }
        });
        documentChunkRepository.saveAll(chunks);

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioBucketName)
                            .object(document.getStorageKey())
                            .build()
            );
        } catch (Exception e) {
            log.error("Error deleting file from MinIO: {}", e.getMessage(), e);
        }

        log.info("Document {} soft deleted", documentId);
    }

    private String extractTextFromDocument(Document document) {
        TextExtractionService extractionService = textExtractionServices.stream()
                .filter(service -> service.supports(document.getMimeType()))
                .findFirst()
                .orElseThrow(() -> new UnsupportedDocumentTypeException(document.getMimeType()));

        try (InputStream inputStream = downloadFromMinIO(document.getStorageKey())) {
            return extractionService.extractText(inputStream, document.getMimeType());
        } catch (Exception e) {
            log.error("Error extracting text from document {}: {}", document.getId(), e.getMessage(), e);
            throw new DocumentProcessingException("Failed to extract text from document", e);
        }
    }

    @CircuitBreaker(name = "minioService", fallbackMethod = "uploadToMinIOFallback")
    @Retry(name = "minioService")
    private void uploadToMinIO(MultipartFile file, String storageKey) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(minioBucketName)
                        .object(storageKey)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
        log.debug("Uploaded file to MinIO with key: {}", storageKey);
    }

    @CircuitBreaker(name = "minioService", fallbackMethod = "downloadFromMinIOFallback")
    @Retry(name = "minioService")
    private InputStream downloadFromMinIO(String storageKey) throws Exception {
        return minioClient.getObject(
                io.minio.GetObjectArgs.builder()
                        .bucket(minioBucketName)
                        .object(storageKey)
                        .build()
        );
    }

    private String generateStorageKey(UUID userId, String originalFilename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String sanitizedFilename = originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
        return String.format("%s/%s_%s", userId, timestamp, sanitizedFilename);
    }

    private void publishDocumentProcessedEvent(Document document) {
        DocumentProcessedEvent event = DocumentProcessedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("DocumentProcessed")
                .version("1.0")
                .occurredAt(Instant.now())
                .correlationId(document.getId().toString())
                .payload(DocumentProcessedEvent.Payload.builder()
                        .documentId(document.getId())
                        .userId(document.getUserId())
                        .originalFilename(document.getOriginalFilename())
                        .documentType(document.getDocumentType())
                        .totalChunks(document.getTotalChunks())
                        .fileSizeBytes(document.getFileSizeBytes())
                        .storageKey(document.getStorageKey())
                        .build())
                .build();

        kafkaTemplate.send(topicProcessed, document.getId().toString(), event);
        log.info("Published DocumentProcessedEvent for document: {}", document.getId());
    }

    private void publishDocumentProcessingFailedEvent(Document document, String errorMessage) {
        DocumentProcessingFailedEvent event = DocumentProcessingFailedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("DocumentProcessingFailed")
                .version("1.0")
                .occurredAt(Instant.now())
                .correlationId(document.getId().toString())
                .payload(DocumentProcessingFailedEvent.Payload.builder()
                        .documentId(document.getId())
                        .userId(document.getUserId())
                        .originalFilename(document.getOriginalFilename())
                        .documentType(document.getDocumentType())
                        .errorMessage(errorMessage)
                        .errorCode("PROCESSING_ERROR")
                        .build())
                .build();

        kafkaTemplate.send(topicProcessingFailed, document.getId().toString(), event);
        log.info("Published DocumentProcessingFailedEvent for document: {}", document.getId());
    }

    private void uploadToMinIOFallback(MultipartFile file, String storageKey, Exception ex) throws Exception {
        log.error("MinIO upload fallback triggered. Error: {}", ex.getMessage());
        throw new DocumentProcessingException("MinIO service unavailable", ex);
    }

    private InputStream downloadFromMinIOFallback(String storageKey, Exception ex) throws Exception {
        log.error("MinIO download fallback triggered. Error: {}", ex.getMessage());
        throw new DocumentProcessingException("MinIO service unavailable", ex);
    }

    private String getDocumentDownloadUrlFallback(UUID documentId, UUID userId, Exception ex) {
        log.error("MinIO pre-signed URL fallback triggered. Error: {}", ex.getMessage());
        throw new DocumentProcessingException("MinIO service unavailable", ex);
    }
}
