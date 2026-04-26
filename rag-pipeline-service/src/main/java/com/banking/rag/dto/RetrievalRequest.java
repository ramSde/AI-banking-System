package com.banking.rag.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record RetrievalRequest(
        @NotBlank(message = "Query text is required")
        @Size(min = 3, max = 5000, message = "Query text must be between 3 and 5000 characters")
        String queryText,

        UUID sessionId,

        @Min(value = 1, message = "Top K must be at least 1")
        @Max(value = 50, message = "Top K cannot exceed 50")
        Integer topK,

        @DecimalMin(value = "0.0", message = "Similarity threshold must be at least 0.0")
        @DecimalMax(value = "1.0", message = "Similarity threshold cannot exceed 1.0")
        BigDecimal similarityThreshold,

        Boolean rerankEnabled,

        @Min(value = 100, message = "Max context tokens must be at least 100")
        @Max(value = 8000, message = "Max context tokens cannot exceed 8000")
        Integer maxContextTokens
) {
    public RetrievalRequest {
        if (topK == null) {
            topK = 10;
        }
        if (similarityThreshold == null) {
            similarityThreshold = new BigDecimal("0.7");
        }
        if (rerankEnabled == null) {
            rerankEnabled = true;
        }
        if (maxContextTokens == null) {
            maxContextTokens = 4000;
        }
    }
}
