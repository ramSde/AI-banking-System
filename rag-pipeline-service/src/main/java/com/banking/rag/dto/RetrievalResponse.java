package com.banking.rag.dto;

import com.banking.rag.domain.RagSource;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RetrievalResponse {

    private UUID queryId;
    
    private String queryText;
    
    private List<RagSource> sources;
    
    private Integer resultsCount;
    
    private Boolean cacheHit;
    
    private Long retrievalLatencyMs;
    
    private Long rerankingLatencyMs;
    
    private Long totalLatencyMs;
    
    private String status;
}
