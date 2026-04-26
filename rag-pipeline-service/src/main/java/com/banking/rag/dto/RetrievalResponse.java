package com.banking.rag.dto;

import java.util.List;
import java.util.UUID;

public record RetrievalResponse(
        UUID queryId,
        UUID contextId,
        String assembledContext,
        Integer totalTokens,
        Integer documentCount,
        List<DocumentSource> sources,
        Boolean cacheHit,
        Long retrievalLatencyMs,
        Long rerankLatencyMs,
        Long totalLatencyMs,
        String traceId
) {
}
