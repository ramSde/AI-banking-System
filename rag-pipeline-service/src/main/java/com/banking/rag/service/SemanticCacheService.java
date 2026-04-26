package com.banking.rag.service;

import com.banking.rag.dto.CacheStatsResponse;
import com.banking.rag.dto.RetrievalResponse;

import java.util.Optional;
import java.util.UUID;

public interface SemanticCacheService {

    Optional<RetrievalResponse> getCachedResult(String queryText, String queryEmbedding);

    void cacheResult(String queryText, String queryEmbedding, UUID contextId, int ttlSeconds);

    void clearCache();

    void clearExpiredCaches();

    CacheStatsResponse getCacheStats();
}
