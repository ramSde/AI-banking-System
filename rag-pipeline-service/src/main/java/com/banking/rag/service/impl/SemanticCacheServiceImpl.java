package com.banking.rag.service.impl;

import com.banking.rag.config.RagProperties;
import com.banking.rag.domain.RagCache;
import com.banking.rag.domain.RagContext;
import com.banking.rag.dto.CacheStatsResponse;
import com.banking.rag.dto.DocumentSource;
import com.banking.rag.dto.RetrievalResponse;
import com.banking.rag.repository.RagCacheRepository;
import com.banking.rag.repository.RagContextRepository;
import com.banking.rag.service.SemanticCacheService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SemanticCacheServiceImpl implements SemanticCacheService {

    private final RagCacheRepository cacheRepository;
    private final RagContextRepository contextRepository;
    private final RagProperties ragProperties;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<RetrievalResponse> getCachedResult(String queryText, String queryEmbedding) {
        log.debug("Checking semantic cache for query: {}", queryText);

        Optional<RagCache> cacheEntry = cacheRepository.findMostSimilarCache(queryEmbedding);

        if (cacheEntry.isEmpty()) {
            log.debug("No cache entry found");
            return Optional.empty();
        }

        RagCache cache = cacheEntry.get();
        cacheRepository.incrementHitCount(cache.getId(), Instant.now());

        Optional<RagContext> context = contextRepository.findByIdAndDeletedAtIsNull(cache.getCachedContextId());

        if (context.isEmpty()) {
            log.warn("Cached context not found: {}", cache.getCachedContextId());
            return Optional.empty();
        }

        RagContext ragContext = context.get();

        try {
            List<DocumentSource> sources = objectMapper.readValue(
                    ragContext.getSources().toString(),
                    new TypeReference<List<DocumentSource>>() {}
            );

            RetrievalResponse response = new RetrievalResponse(
                    ragContext.getQueryId(),
                    ragContext.getId(),
                    ragContext.getAssembledContext(),
                    ragContext.getTotalTokens(),
                    ragContext.getDocumentCount(),
                    sources,
                    true,
                    0L,
                    0L,
                    0L,
                    UUID.randomUUID().toString()
            );

            log.info("Cache hit for query: {}", queryText);
            return Optional.of(response);

        } catch (Exception e) {
            log.error("Failed to deserialize cached context", e);
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public void cacheResult(String queryText, String queryEmbedding, UUID contextId, int ttlSeconds) {
        log.debug("Caching result for context: {}", contextId);

        Instant expiresAt = Instant.now().plusSeconds(ttlSeconds);

        RagCache cache = RagCache.builder()
                .queryText(queryText)
                .queryEmbedding(queryEmbedding)
                .cachedContextId(contextId)
                .hitCount(0)
                .lastHitAt(Instant.now())
                .expiresAt(expiresAt)
                .build();

        cacheRepository.save(cache);
        log.info("Cached result for context: {}", contextId);
    }

    @Override
    @Transactional
    public void clearCache() {
        log.info("Clearing all cache entries");
        List<RagCache> allCaches = cacheRepository.findAll();
        allCaches.forEach(cache -> cache.setDeletedAt(Instant.now()));
        cacheRepository.saveAll(allCaches);
        log.info("Cleared {} cache entries", allCaches.size());
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 * * * *")
    public void clearExpiredCaches() {
        log.debug("Clearing expired cache entries");
        int deleted = cacheRepository.deleteExpiredCaches(Instant.now());
        if (deleted > 0) {
            log.info("Cleared {} expired cache entries", deleted);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CacheStatsResponse getCacheStats() {
        Long totalEntries = cacheRepository.countActiveCaches(Instant.now());
        Long totalHits = cacheRepository.getTotalCacheHits();

        BigDecimal hitRate = BigDecimal.ZERO;
        Long avgHitsPerEntry = 0L;

        if (totalEntries != null && totalEntries > 0) {
            if (totalHits != null) {
                hitRate = BigDecimal.valueOf(totalHits)
                        .divide(BigDecimal.valueOf(totalEntries), 4, RoundingMode.HALF_UP);
                avgHitsPerEntry = totalHits / totalEntries;
            }
        }

        return new CacheStatsResponse(
                totalEntries != null ? totalEntries : 0L,
                totalHits != null ? totalHits : 0L,
                hitRate,
                avgHitsPerEntry
        );
    }
}
