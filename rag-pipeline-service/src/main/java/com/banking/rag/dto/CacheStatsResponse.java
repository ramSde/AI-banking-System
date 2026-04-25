package com.banking.rag.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CacheStatsResponse {

    private Long totalEntries;
    
    private Long activeEntries;
    
    private Long expiredEntries;
    
    private Long totalHits;
    
    private BigDecimal hitRate;
    
    private BigDecimal averageHitsPerEntry;
}
