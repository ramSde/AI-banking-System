package com.banking.rag.service.impl;

import com.banking.rag.domain.RagContext;
import com.banking.rag.domain.RagQuery;
import com.banking.rag.domain.RagSource;
import com.banking.rag.dto.RetrievalRequest;
import com.banking.rag.dto.RetrievalResponse;
import com.banking.rag.event.RetrievalCompletedEvent;
import com.banking.rag.event.RetrievalFailedEvent;
import com.banking.rag.event.RetrievalRequestedEvent;
import com.banking.rag.exception.RagException;
import com.banking.rag.mapper.RagQueryMapper;
import com.banking.rag.repository.RagQueryRepository;
import com.banking.rag.service.*;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class RagServiceImpl implements RagService {

    private final RagQueryRepository ragQueryRepository;
    private final RetrievalService retrievalService;
    private final RerankingService rerankingService;
    private final ContextAssemblyService contextAssemblyService;
    private final SemanticCacheService semanticCacheService;
    private final RagQueryMapper ragQueryMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Tracer tracer;

    @Value("${rag.reranking.top-n:5}")
    private int rerankingTopN;

    @Value("${rag.context.max-tokens:4000}")
    private int maxContextTokens;

    @Value("${rag.cache.ttl-seconds:3600}")
    private long cacheTtlSeconds;

    @Value("${rag.cache.similarity-threshold:0.95}")
    private String cacheSimilarityThreshold;

    public RagServiceImpl(
            RagQueryRepository ragQueryRepository,
            RetrievalService retrievalService,
            RerankingService rerankingService,
            ContextAssemblyService contextAssemblyService,
            SemanticCacheService semanticCacheService,
            RagQueryMapper ragQueryMapper,
            KafkaTemplate<String, Object> kafkaTemplate,
            Tracer tracer) {
        this.ragQueryRepository = ragQueryRepository;
        this.retrievalService = retrievalService;
        this.rerankingService = rerankingService;
        this.contextAssemblyService = contextAssemblyService;
        this.semanticCacheService = semanticCacheService;
        this.ragQueryMapper = ragQueryMapper;
        this.kafkaTemplate = kafkaTemplate;
        this.tracer = tracer;
    }

    @Override
    @Transactional
    public RetrievalResponse retrieve(RetrievalRequest request) {
        long startTime = System.currentTimeMillis();
        String traceId = getTraceId();

        RagQuery ragQuery = RagQuery.builder()
                .userId(request.getUserId())
                .queryText(request.getQueryText())
                .topK(request.getTopK())
                .similarityThreshold(request.getSimilarityThreshold())
                .rerankingEnabled(request.getEnableReranking())
                .traceId(traceId)
                .sessionId(request.getSessionId())
                .status("PENDING")
                .build();

        ragQuery = ragQueryRepository.save(ragQuery);

        publishRetrievalRequestedEvent(ragQuery);

        try {
            float[] queryEmbedding = retrievalService.generateEmbedding(request.getQueryText()).join();
            ragQuery.setQueryEmbedding(serializeEmbedding(queryEmbedding));

            RetrievalResponse cachedResponse = null;
            if (Boolean.TRUE.equals(request.getEnableCache())) {
                cachedResponse = semanticCacheService.getCachedResponse(
                        queryEmbedding,
                        new java.math.BigDecimal(cacheSimilarityThreshold)
                );
            }

            if (cachedResponse != null) {
                ragQuery.setCacheHit(true);
                ragQuery.setStatus("COMPLETED");
                ragQuery.setTotalLatencyMs(System.currentTimeMillis() - startTime);
                ragQueryRepository.save(ragQuery);

                log.info("Returning cached response for query {}", ragQuery.getId());
                return cachedResponse;
            }

            long retrievalStart = System.currentTimeMillis();
            List<RagSource> sources = retrievalService.retrieve(
                    request.getQueryText(),
                    request.getTopK(),
                    request.getSimilarityThreshold()
            ).join();
            long retrievalLatency = System.currentTimeMillis() - retrievalStart;

            long rerankingLatency = 0L;
            if (Boolean.TRUE.equals(request.getEnableReranking()) && !sources.isEmpty()) {
                long rerankingStart = System.currentTimeMillis();
                sources = rerankingService.rerank(request.getQueryText(), sources, rerankingTopN);
                rerankingLatency = System.currentTimeMillis() - rerankingStart;
            }

            RagContext context = contextAssemblyService.assembleContext(
                    ragQuery.getId(),
                    request.getQueryText(),
                    sources,
                    maxContextTokens,
                    true
            );

            ragQuery.setRetrievalLatencyMs(retrievalLatency);
            ragQuery.setRerankingLatencyMs(rerankingLatency);
            ragQuery.setTotalLatencyMs(System.currentTimeMillis() - startTime);
            ragQuery.setResultsCount(sources.size());
            ragQuery.setStatus("COMPLETED");
            ragQuery = ragQueryRepository.save(ragQuery);

            RetrievalResponse response = ragQueryMapper.toRetrievalResponse(ragQuery);
            response.setSources(sources);

            if (Boolean.TRUE.equals(request.getEnableCache())) {
                semanticCacheService.cacheResponse(
                        request.getQueryText(),
                        queryEmbedding,
                        response,
                        cacheTtlSeconds
                );
            }

            publishRetrievalCompletedEvent(ragQuery, context.getId());

            log.info("RAG retrieval completed for query {} in {}ms", ragQuery.getId(), ragQuery.getTotalLatencyMs());
            return response;

        } catch (Exception e) {
            log.error("RAG retrieval failed for query {}: {}", ragQuery.getId(), e.getMessage(), e);
            
            ragQuery.setStatus("FAILED");
            ragQuery.setErrorMessage(e.getMessage());
            ragQuery.setTotalLatencyMs(System.currentTimeMillis() - startTime);
            ragQueryRepository.save(ragQuery);

            publishRetrievalFailedEvent(ragQuery, e);

            throw new RagException("RAG retrieval failed", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public RetrievalResponse getQueryById(UUID queryId) {
        RagQuery ragQuery = ragQueryRepository.findByIdAndNotDeleted(queryId)
                .orElseThrow(() -> new RagException("Query not found: " + queryId));

        return ragQueryMapper.toRetrievalResponse(ragQuery);
    }

    private void publishRetrievalRequestedEvent(RagQuery ragQuery) {
        try {
            RetrievalRequestedEvent event = RetrievalRequestedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("RetrievalRequested")
                    .version("1.0")
                    .occurredAt(Instant.now())
                    .correlationId(ragQuery.getTraceId())
                    .payload(RetrievalRequestedEvent.Payload.builder()
                            .queryId(ragQuery.getId())
                            .userId(ragQuery.getUserId())
                            .queryText(ragQuery.getQueryText())
                            .topK(ragQuery.getTopK())
                            .similarityThreshold(ragQuery.getSimilarityThreshold())
                            .rerankingEnabled(ragQuery.getRerankingEnabled())
                            .sessionId(ragQuery.getSessionId())
                            .traceId(ragQuery.getTraceId())
                            .build())
                    .build();

            kafkaTemplate.send("banking.rag.retrieval-requested", event);
        } catch (Exception e) {
            log.error("Failed to publish retrieval requested event: {}", e.getMessage());
        }
    }

    private void publishRetrievalCompletedEvent(RagQuery ragQuery, UUID contextId) {
        try {
            RetrievalCompletedEvent event = RetrievalCompletedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("RetrievalCompleted")
                    .version("1.0")
                    .occurredAt(Instant.now())
                    .correlationId(ragQuery.getTraceId())
                    .payload(RetrievalCompletedEvent.Payload.builder()
                            .queryId(ragQuery.getId())
                            .userId(ragQuery.getUserId())
                            .contextId(contextId)
                            .resultsCount(ragQuery.getResultsCount())
                            .cacheHit(ragQuery.getCacheHit())
                            .retrievalLatencyMs(ragQuery.getRetrievalLatencyMs())
                            .rerankingLatencyMs(ragQuery.getRerankingLatencyMs())
                            .totalLatencyMs(ragQuery.getTotalLatencyMs())
                            .traceId(ragQuery.getTraceId())
                            .build())
                    .build();

            kafkaTemplate.send("banking.rag.retrieval-completed", event);
        } catch (Exception e) {
            log.error("Failed to publish retrieval completed event: {}", e.getMessage());
        }
    }

    private void publishRetrievalFailedEvent(RagQuery ragQuery, Exception exception) {
        try {
            RetrievalFailedEvent event = RetrievalFailedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("RetrievalFailed")
                    .version("1.0")
                    .occurredAt(Instant.now())
                    .correlationId(ragQuery.getTraceId())
                    .payload(RetrievalFailedEvent.Payload.builder()
                            .queryId(ragQuery.getId())
                            .userId(ragQuery.getUserId())
                            .queryText(ragQuery.getQueryText())
                            .errorCode("RETRIEVAL_FAILED")
                            .errorMessage(exception.getMessage())
                            .attemptedLatencyMs(ragQuery.getTotalLatencyMs())
                            .traceId(ragQuery.getTraceId())
                            .build())
                    .build();

            kafkaTemplate.send("banking.rag.retrieval-failed", event);
        } catch (Exception e) {
            log.error("Failed to publish retrieval failed event: {}", e.getMessage());
        }
    }

    private String getTraceId() {
        if (tracer != null && tracer.currentSpan() != null && tracer.currentSpan().context() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return UUID.randomUUID().toString();
    }

    private byte[] serializeEmbedding(float[] embedding) {
        byte[] bytes = new byte[embedding.length * 4];
        for (int i = 0; i < embedding.length; i++) {
            int bits = Float.floatToIntBits(embedding[i]);
            bytes[i * 4] = (byte) (bits >> 24);
            bytes[i * 4 + 1] = (byte) (bits >> 16);
            bytes[i * 4 + 2] = (byte) (bits >> 8);
            bytes[i * 4 + 3] = (byte) bits;
        }
        return bytes;
    }
}
