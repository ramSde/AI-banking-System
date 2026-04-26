package com.banking.rag.dto;

import java.util.List;

public record RerankResponse(
        List<RankedDocument> rankedDocuments,
        Long latencyMs
) {
}
