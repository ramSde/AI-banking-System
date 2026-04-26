package com.banking.rag.dto;

import java.math.BigDecimal;

public record CacheStatsResponse(
        Long totalCacheEntries,
        Long totalCacheHits,
        BigDecimal cacheHitRate,
        Long averageHitsPerEntry
) {
}
