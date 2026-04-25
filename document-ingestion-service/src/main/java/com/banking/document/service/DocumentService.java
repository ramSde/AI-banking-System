package com.banking.document.service;

import com.banking.document.domain.Document;
import com.banking.document.domain.DocumentType;
import com.banking.document.domain.ProcessingStatus;
import com.banking.document.dto.DocumentResponse;
import com.banking.document.dto.DocumentSearchRequest;
import com.banking.document.dto.DocumentStatsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface DocumentService {

    DocumentResponse uploadDocument(MultipartFile file, DocumentType documentType, UUID userId);

    void processDocument(UUID documentId);

    DocumentResponse getDocument(UUID documentId, UUID userId);

    Page<DocumentResponse> getUserDocuments(UUID userId, Pageable pageable);

    Page<DocumentResponse> getUserDocumentsByStatus(UUID userId, ProcessingStatus status, Pageable pageable);

    Page<DocumentResponse> getUserDocumentsByType(UUID userId, DocumentType documentType, Pageable pageable);

    Page<DocumentResponse> getUserDocumentsByDateRange(UUID userId, Instant startDate, Instant endDate, Pageable pageable);

    List<DocumentResponse> searchDocuments(DocumentSearchRequest request, UUID userId);

    DocumentStatsResponse getUserDocumentStats(UUID userId);

    String getDocumentDownloadUrl(UUID documentId, UUID userId);

    void deleteDocument(UUID documentId, UUID userId);
}
