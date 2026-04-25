package com.banking.rag.dto;

import com.banking.rag.domain.RagSource;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContextAssemblyRequest {

    @NotNull(message = "Query ID is required")
    private UUID queryId;

    @NotBlank(message = "Query text is required")
    private String queryText;

    @NotEmpty(message = "Sources list cannot be empty")
    private List<RagSource> sources;

    @Min(value = 100, message = "Max tokens must be at least 100")
    @Max(value = 32000, message = "Max tokens cannot exceed 32000")
    @Builder.Default
    private Integer maxTokens = 4000;

    @Builder.Default
    private Boolean includeSources = true;
}
