package com.banking.document.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentStatsResponse {

    private Long totalDocuments;
    private Long pendingDocuments;
    private Long processingDocuments;
    private Long completedDocuments;
    private Long failedDocuments;
    private Long totalChunks;
    private Long totalSizeBytes;
    private Map<String, Long> documentsByType;
}
