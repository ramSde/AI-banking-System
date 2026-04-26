package com.banking.rag.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record DocumentSource(
        UUID documentId,
        UUID chunkId,
        String title,
        String content,
        Integer page,
        BigDecimal similarityScore,
        BigDecimal rerankScore,
        String documentType,
        String metadata
) {
}
