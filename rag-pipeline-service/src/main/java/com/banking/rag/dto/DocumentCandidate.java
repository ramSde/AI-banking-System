package com.banking.rag.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record DocumentCandidate(
        UUID documentId,
        UUID chunkId,
        String content,
        BigDecimal initialScore,
        String metadata
) {
}
