package com.banking.document.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentSearchRequest {

    @NotBlank(message = "Query text is required")
    private String query;

    @Min(value = 1, message = "Top K must be at least 1")
    @Max(value = 100, message = "Top K cannot exceed 100")
    @Builder.Default
    private Integer topK = 5;

    @Min(value = 0, message = "Similarity threshold must be between 0 and 1")
    @Max(value = 1, message = "Similarity threshold must be between 0 and 1")
    @Builder.Default
    private Double similarityThreshold = 0.7;
}
