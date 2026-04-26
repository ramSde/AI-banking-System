package com.banking.rag.dto;

import jakarta.validation.constraints.*;

import java.util.List;

public record RerankRequest(
        @NotBlank(message = "Query text is required")
        String queryText,

        @NotEmpty(message = "Documents list cannot be empty")
        @Size(min = 1, max = 100, message = "Documents list must contain between 1 and 100 items")
        List<DocumentCandidate> documents,

        @Min(value = 1, message = "Top N must be at least 1")
        @Max(value = 20, message = "Top N cannot exceed 20")
        Integer topN
) {
    public RerankRequest {
        if (topN == null) {
            topN = 5;
        }
    }
}
