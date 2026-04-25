package com.banking.rag.dto;

import com.banking.rag.domain.RagSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RerankRequest {

    @NotBlank(message = "Query text is required")
    @Size(min = 3, max = 5000, message = "Query text must be between 3 and 5000 characters")
    private String queryText;

    @NotEmpty(message = "Sources list cannot be empty")
    private List<RagSource> sources;

    @Builder.Default
    private Integer topN = 5;
}
