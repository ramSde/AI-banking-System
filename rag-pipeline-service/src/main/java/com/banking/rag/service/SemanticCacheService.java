package com.banking.rag.service;

import com.banking.rag.dto.CacheStatsResponse;
import com.banking.rag.dto.RetrievalResponse;

import java.math.BigDecimal;

public interface SemanticCacheService {

    /**
     * Checks if a similar query exists in cache.
     * 
     * @param queryEmbedding Query embedding
     * @param similarityThreshold Similarity threshold
     * @return Cached response if found, null otherwise
     */
    RetrievalResponse getCachedResponse(float[] queryEmbedding, BigDecimal similarityThreshold);

    /**
     * Stores query response in cache.
     * 
     * @param queryText Query text
     * @param queryEmbedding Query embedding
     * @param response Response to cache
     * @param ttlSeconds Time to live in seconds
     */
    void cacheResponse(String queryText, float[] queryEmbedding, RetrievalResponse response, long ttlSeconds);

    /**
     * Gets cache statistics.
     * 
     * @return Cache statistics
     */
    CacheStatsResponse getCacheStats();

    /**
     * Clears all cache entries.
     */
    void clearCache();

    /**
     * Removes expired cache entries.
     * 
     * @return Number of entries removed
     */
    int removeExpiredEntries();
}
