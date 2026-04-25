package com.banking.rag.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetrievalRequest {

    @NotBlank(message = "Query text is required")
    @Size(min = 3, max = 5000, message = "Query text must be between 3 and 5000 characters")
    private String queryText;

    @Min(value = 1, message = "Top K must be at least 1")
    @Max(value = 50, message = "Top K cannot exceed 50")
    @Builder.Default
    private Integer topK = 10;

    @DecimalMin(value = "0.0", message = "Similarity threshold must be at least 0.0")
    @DecimalMax(value = "1.0", message = "Similarity threshold cannot exceed 1.0")
    @Builder.Default
    private BigDecimal similarityThreshold = new BigDecimal("0.7");

    @Builder.Default
    private Boolean enableReranking = true;

    @Builder.Default
    private Boolean enableCache = true;

    private UUID userId;

    private String sessionId;
}
