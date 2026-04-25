package com.banking.rag.service.impl;

import com.banking.rag.domain.RagCache;
import com.banking.rag.dto.CacheStatsResponse;
import com.banking.rag.dto.RetrievalResponse;
import com.banking.rag.repository.RagCacheRepository;
import com.banking.rag.service.SemanticCacheService;
import com.banking.rag.util.SimilarityCalculator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RedisSemanticCacheService implements SemanticCacheService {

    private final RagCacheRepository ragCacheRepository;
    private final SimilarityCalculator similarityCalculator;
    private final ObjectMapper objectMapper;

    public RedisSemanticCacheService(
            RagCacheRepository ragCacheRepository,
            SimilarityCalculator similarityCalculator,
            ObjectMapper objectMapper) {
        this.ragCacheRepository = ragCacheRepository;
        this.similarityCalculator = similarityCalculator;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public RetrievalResponse getCachedResponse(float[] queryEmbedding, BigDecimal similarityThreshold) {
        try {
            log.debug("Checking semantic cache for similar queries");

            List<RagCache> activeCaches = ragCacheRepository
                    .findActiveEntriesOrderByHitCount(Instant.now(), PageRequest.of(0, 100))
                    .getContent();

            for (RagCache cache : activeCaches) {
                float[] cachedEmbedding = deserializeEmbedding(cache.getQueryEmbedding());
                BigDecimal similarity = similarityCalculator.cosineSimilarity(queryEmbedding, cachedEmbedding);

                if (similarity.compareTo(similarityThreshold) >= 0) {
                    log.info("Cache hit with similarity: {}", similarity);
                    ragCacheRepository.incrementHitCount(cache.getId(), Instant.now());
                    
                    return objectMapper.convertValue(cache.getCachedResponse(), RetrievalResponse.class);
                }
            }

            log.debug("No cache hit found");
            return null;

        } catch (Exception e) {
            log.error("Error checking cache: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    @Transactional
    public void cacheResponse(String queryText, float[] queryEmbedding, RetrievalResponse response, long ttlSeconds) {
        try {
            log.debug("Caching response for query");

            Map<String, Object> cachedResponse = objectMapper.convertValue(response, Map.class);

            RagCache cache = RagCache.builder()
                    .queryText(queryText)
                    .queryEmbedding(serializeEmbedding(queryEmbedding))
                    .cachedResponse(cachedResponse)
                    .hitCount(0)
                    .expiresAt(Instant.now().plusSeconds(ttlSeconds))
                    .build();

            ragCacheRepository.save(cache);
            log.info("Response cached successfully");

        } catch (Exception e) {
            log.error("Error caching response: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CacheStatsResponse getCacheStats() {
        try {
            Instant now = Instant.now();
            
            Long totalEntries = ragCacheRepository.count();
            Long activeEntries = ragCacheRepository.countActiveEntries(now);
            Long expiredEntries = totalEntries - activeEntries;
            Long totalHits = ragCacheRepository.getTotalHitCount();

            BigDecimal hitRate = BigDecimal.ZERO;
            if (totalEntries > 0) {
                hitRate = BigDecimal.valueOf(totalHits)
                        .divide(BigDecimal.valueOf(totalEntries), 4, RoundingMode.HALF_UP);
            }

            BigDecimal averageHitsPerEntry = BigDecimal.ZERO;
            if (activeEntries > 0) {
                averageHitsPerEntry = BigDecimal.valueOf(totalHits)
                        .divide(BigDecimal.valueOf(activeEntries), 2, RoundingMode.HALF_UP);
            }

            return CacheStatsResponse.builder()
                    .totalEntries(totalEntries)
                    .activeEntries(activeEntries)
                    .expiredEntries(expiredEntries)
                    .totalHits(totalHits)
                    .hitRate(hitRate)
                    .averageHitsPerEntry(averageHitsPerEntry)
                    .build();

        } catch (Exception e) {
            log.error("Error getting cache stats: {}", e.getMessage(), e);
            return CacheStatsResponse.builder().build();
        }
    }

    @Override
    @Transactional
    public void clearCache() {
        try {
            int cleared = ragCacheRepository.clearAllCache(Instant.now());
            log.info("Cleared {} cache entries", cleared);
        } catch (Exception e) {
            log.error("Error clearing cache: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public int removeExpiredEntries() {
        try {
            int removed = ragCacheRepository.softDeleteExpiredEntries(Instant.now());
            log.info("Removed {} expired cache entries", removed);
            return removed;
        } catch (Exception e) {
            log.error("Error removing expired entries: {}", e.getMessage(), e);
            return 0;
        }
    }

    private byte[] serializeEmbedding(float[] embedding) {
        try {
            return objectMapper.writeValueAsBytes(embedding);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize embedding", e);
        }
    }

    private float[] deserializeEmbedding(byte[] bytes) {
        try {
            return objectMapper.readValue(bytes, float[].class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize embedding", e);
        }
    }
}
