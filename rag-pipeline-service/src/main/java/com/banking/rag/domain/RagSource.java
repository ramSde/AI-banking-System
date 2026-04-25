package com.banking.rag.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Value object representing a source document in RAG retrieval results.
 * This is not a JPA entity - it's used for in-memory representation and JSON serialization.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagSource {

    private UUID documentId;
    
    private String documentName;
    
    private String chunkId;
    
    private String content;
    
    private BigDecimal similarityScore;
    
    private BigDecimal rerankScore;
    
    private Integer rank;
    
    private Map<String, Object> metadata;
}
