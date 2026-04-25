package com.banking.document.dto;

import com.banking.document.domain.DocumentType;
import com.banking.document.domain.ProcessingStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentResponse {

    private UUID id;
    private UUID userId;
    private String originalFilename;
    private String mimeType;
    private Long fileSizeBytes;
    private DocumentType documentType;
    private ProcessingStatus processingStatus;
    private Integer totalChunks;
    private String errorMessage;
    private Map<String, Object> metadata;
    private Instant createdAt;
    private Instant updatedAt;
    private String downloadUrl;
}
