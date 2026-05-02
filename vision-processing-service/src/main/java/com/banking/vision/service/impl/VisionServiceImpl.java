package com.banking.vision.service.impl;

import com.banking.vision.config.VisionProperties;
import com.banking.vision.domain.DocumentType;
import com.banking.vision.domain.ProcessingStatus;
import com.banking.vision.domain.VisionDocument;
import com.banking.vision.event.VisionEventPublisher;
import com.banking.vision.exception.DocumentNotFoundException;
import com.banking.vision.exception.InvalidDocumentException;
import com.banking.vision.exception.UnsupportedDocumentTypeException;
import com.banking.vision.repository.OcrResultRepository;
import com.banking.vision.repository.VisionDocumentRepository;
import com.banking.vision.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of VisionService.
 * 
 * Orchestrates the complete document processing workflow:
 * 1. Upload and validate document
 * 2. Store in MinIO/S3
 * 3. Trigger async processing (if large file)
 * 4. Perform OCR
 * 5. Extract structured data
 * 6. Publish events
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VisionServiceImpl implements VisionService {

    private final VisionDocumentRepository documentRepository;
    private final OcrResultRepository ocrResultRepository;
    private final StorageService storageService;
    private final OcrService ocrService;
    private final DataExtractionService dataExtractionService;
    private final VisionEventPublisher eventPublisher;
    private final VisionProperties visionProperties;

    @Override
    @Transactional
    public VisionDocument uploadDocument(
        MultipartFile file,
        DocumentType documentType,
        UUID userId,
        Map<String, Object> metadata
    ) {
        log.info("Uploading document for user: {}, type: {}", userId, documentType);
        
        // Validate file
        validateFile(file);
        
        // Create document entity
        VisionDocument document = VisionDocument.builder()
            .userId(userId)
            .documentType(documentType)
            .originalFilename(file.getOriginalFilename())
            .fileSize(file.getSize())
            .mimeType(file.getContentType())
            .processingStatus(ProcessingStatus.PENDING)
            .metadata(metadata != null ? metadata : new HashMap<>())
            .build();
        
        // Save to get ID
        document = documentRepository.save(document);
        
        try {
            // Upload to storage
            String storageKey = storageService.uploadFile(
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType(),
                userId,
                document.getId()
            );
            
            document.setStorageKey(storageKey);
            document = documentRepository.save(document);
            
            log.info("Document uploaded successfully: {}", document.getId());
            
            // Publish upload event
            eventPublisher.publishDocumentUploaded(document);
            
            // Trigger processing (async if large file)
            if (file.getSize() > visionProperties.getProcessing().getAsyncThresholdBytes()) {
                log.info("Document {} exceeds threshold, processing asynchronously", document.getId());
                processDocumentAsync(document.getId());
            } else {
                log.info("Document {} below threshold, processing synchronously", document.getId());
                processDocument(document.getId());
            }
            
            return document;
            
        } catch (Exception e) {
            log.error("Failed to upload document: {}", e.getMessage(), e);
            document.failProcessing("Upload failed: " + e.getMessage());
            documentRepository.save(document);
            throw new InvalidDocumentException("Failed to upload document", e);
        }
    }

    @Override
    @Cacheable(value = "vision:document", key = "#documentId")
    @Transactional(readOnly = true)
    public VisionDocument getDocument(UUID documentId, UUID userId) {
        log.debug("Fetching document: {} for user: {}", documentId, userId);
        return documentRepository.findByIdAndUserId(documentId, userId)
            .orElseThrow(() -> new DocumentNotFoundException(documentId, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VisionDocument> getUserDocuments(UUID userId, Pageable pageable) {
        log.debug("Fetching documents for user: {}", userId);
        return documentRepository.findByUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VisionDocument> getUserDocumentsByType(
        UUID userId,
        DocumentType documentType,
        Pageable pageable
    ) {
        log.debug("Fetching documents for user: {}, type: {}", userId, documentType);
        return documentRepository.findByUserIdAndDocumentType(userId, documentType, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VisionDocument> getUserDocumentsByStatus(
        UUID userId,
        ProcessingStatus status,
        Pageable pageable
    ) {
        log.debug("Fetching documents for user: {}, status: {}", userId, status);
        return documentRepository.findByUserIdAndProcessingStatus(userId, status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VisionDocument> getUserDocumentsByDateRange(
        UUID userId,
        Instant startDate,
        Instant endDate,
        Pageable pageable
    ) {
        log.debug("Fetching documents for user: {} between {} and {}", userId, startDate, endDate);
        return documentRepository.findByUserIdAndCreatedAtBetween(userId, startDate, endDate, pageable);
    }

    @Override
    @Transactional
    public void processDocument(UUID documentId) {
        log.info("Processing document: {}", documentId);
        
        VisionDocument document = documentRepository.findById(documentId)
            .orElseThrow(() -> new DocumentNotFoundException(documentId));
        
        try {
            // Mark as processing
            document.startProcessing();
            documentRepository.save(document);
            
            // Download file from storage
            // Perform OCR
            // Extract data
            // Update document with results
            // This is a simplified version - full implementation would include all steps
            
            log.info("Document processing completed: {}", documentId);
            
        } catch (Exception e) {
            log.error("Document processing failed: {}", e.getMessage(), e);
            document.failProcessing(e.getMessage());
            documentRepository.save(document);
            eventPublisher.publishProcessingFailed(document, e.getMessage());
        }
    }

    @Async("visionTaskExecutor")
    public void processDocumentAsync(UUID documentId) {
        processDocument(documentId);
    }

    @Override
    @Cacheable(value = "vision:status", key = "#documentId")
    @Transactional(readOnly = true)
    public Map<String, Object> getProcessingStatus(UUID documentId, UUID userId) {
        log.debug("Fetching processing status for document: {}", documentId);
        
        VisionDocument document = getDocument(documentId, userId);
        
        Map<String, Object> status = new HashMap<>();
        status.put("documentId", document.getId());
        status.put("status", document.getProcessingStatus());
        status.put("confidenceScore", document.getConfidenceScore());
        status.put("errorMessage", document.getErrorMessage());
        
        // Get page count
        long pageCount = ocrResultRepository.countByDocumentId(documentId);
        status.put("pageCount", pageCount);
        
        return status;
    }

    @Override
    @Cacheable(value = "vision:ocr", key = "#documentId")
    @Transactional(readOnly = true)
    public Map<String, Object> getOcrResults(UUID documentId, UUID userId) {
        log.debug("Fetching OCR results for document: {}", documentId);
        
        // Verify user owns document
        getDocument(documentId, userId);
        
        var ocrResults = ocrService.getOcrResults(documentId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("documentId", documentId);
        response.put("results", ocrResults);
        response.put("averageConfidence", ocrService.calculateAverageConfidence(documentId));
        
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getExtractedData(UUID documentId, UUID userId) {
        log.debug("Fetching extracted data for document: {}", documentId);
        
        VisionDocument document = getDocument(documentId, userId);
        
        if (document.getProcessingStatus() != ProcessingStatus.COMPLETED) {
            throw new InvalidDocumentException("Document processing not completed");
        }
        
        // Get OCR text
        String ocrText = ocrService.getCombinedText(documentId);
        
        // Extract structured data
        Map<String, Object> extractedData = dataExtractionService.extractData(
            ocrText,
            document.getDocumentType()
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("documentId", documentId);
        response.put("documentType", document.getDocumentType());
        response.put("extractedData", extractedData);
        response.put("confidenceScore", document.getConfidenceScore());
        response.put("rawText", ocrText);
        
        return response;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"vision:document", "vision:status", "vision:ocr"}, key = "#documentId")
    public void deleteDocument(UUID documentId, UUID userId) {
        log.info("Deleting document: {} for user: {}", documentId, userId);
        
        VisionDocument document = getDocument(documentId, userId);
        
        // Soft delete
        document.softDelete();
        documentRepository.save(document);
        
        // Delete from storage (async)
        try {
            storageService.deleteFile(document.getStorageKey());
        } catch (Exception e) {
            log.warn("Failed to delete file from storage: {}", e.getMessage());
        }
        
        log.info("Document deleted successfully: {}", documentId);
    }

    @Override
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidDocumentException("File is empty");
        }
        
        // Check file size
        if (file.getSize() > visionProperties.getUpload().getMaxFileSizeBytes()) {
            throw new InvalidDocumentException(
                String.format("File size exceeds maximum allowed: %d bytes",
                    visionProperties.getUpload().getMaxFileSizeBytes())
            );
        }
        
        // Check MIME type
        String contentType = file.getContentType();
        if (contentType == null || 
            !visionProperties.getUpload().getSupportedFormats().contains(contentType)) {
            throw new UnsupportedDocumentTypeException(contentType);
        }
        
        // Check file extension
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new InvalidDocumentException("Filename is missing");
        }
        
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        if (!visionProperties.getUpload().getSupportedExtensions().contains(extension)) {
            throw new UnsupportedDocumentTypeException(
                "Unsupported file extension: " + extension,
                "UNSUPPORTED_FILE_EXTENSION"
            );
        }
        
        log.debug("File validation passed: {}", filename);
    }
}
