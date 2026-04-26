package com.banking.rag.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record RankedDocument(
        UUID documentId,
        UUID chunkId,
        String content,
        BigDecimal initialScore,
        BigDecimal rerankScore,
        Integer rank,
        String metadata
) {
}
