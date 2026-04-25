package com.banking.rag.dto;

import com.banking.rag.domain.RagSource;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RerankResponse {

    private String queryText;
    
    private List<RagSource> rerankedSources;
    
    private Integer resultsCount;
    
    private Long rerankingLatencyMs;
}
