package com.banking.rag.service.impl;

import com.banking.rag.config.RagProperties;
import com.banking.rag.domain.RagQuery;
import com.banking.rag.dto.*;
import com.banking.rag.event.RagQueryEvent;
import com.banking.rag.exception.VectorSearchException;
import com.banking.rag.repository.RagQueryRepository;
import com.banking.rag.service.ContextAssemblyService;
import com.banking.rag.service.RerankingService;
import com.banking.rag.service.RetrievalService;
import com.banking.rag.service.SemanticCacheService;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.banking.rag.config.KafkaConfig.RAG_QUERY_TOPIC;

@Service
@Slf4j
@RequiredArgsConstructor
public class RetrievalServiceImpl implements RetrievalService {

    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;
    private final RerankingService rerankingService;
    private final ContextAssemblyService contextAssemblyService;
    private final SemanticCacheService semanticCacheService;
    private final RagQueryRepository queryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RagProperties ragProperties;
    private final Tracer tracer;

    @Override
    @Transactional
    public RetrievalResponse retrieve(RetrievalRequest request, UUID userId) {
        long startTime = System.currentTimeMillis();
        String traceId = getTraceId();

        log.info("Processing retrieval request for user: {}, query: {}", userId, request.queryText());

        String queryEmbedding = generateEmbedding(request.queryText());

        if (ragProperties.getCache().getEnabled()) {
            Optional<RetrievalResponse> cachedResult = semanticCacheService.getCachedResult(
                    request.queryText(), queryEmbedding);
            if (cachedResult.isPresent()) {
                log.info("Cache hit for query: {}", request.queryText());
                return cachedResult.get();
            }
        }

        long retrievalStart = System.currentTimeMillis();
        List<DocumentCandidate> candidates = searchVectorStore(
                request.queryText(),
                request.topK(),
                request.similarityThreshold().doubleValue()
        );
        long retrievalLatency = System.currentTimeMillis() - retrievalStart;

        List<RankedDocument> rankedDocuments;
        long rerankLatency = 0L;

        if (request.rerankEnabled() && ragProperties.getReranking().getEnabled()) {
            long rerankStart = System.currentTimeMillis();
            rankedDocuments = rerankingService.rerankDocuments(
                    request.queryText(),
                    candidates,
                    ragProperties.getReranking().getTopN()
            );
            rerankLatency = System.currentTimeMillis() - rerankStart;
        } else {
            rankedDocuments = candidates.stream()
                    .limit(ragProperties.getReranking().getTopN())
                    .map(c -> new RankedDocument(
                            c.documentId(), c.chunkId(), c.content(),
                            c.initialScore(), c.initialScore(), 0, c.metadata()
                    ))
                    .collect(Collectors.toList());
        }

        String assembledContext = contextAssemblyService.assembleContext(
                rankedDocuments, request.maxContextTokens());
        int totalTokens = contextAssemblyService.countTokens(assembledContext);
        List<DocumentSource> sources = contextAssemblyService.extractSources(rankedDocuments);

        RagQuery query = RagQuery.builder()
                .userId(userId)
                .sessionId(request.sessionId())
                .queryText(request.queryText())
                .queryEmbedding(queryEmbedding)
                .topK(request.topK())
                .similarityThreshold(request.similarityThreshold())
                .rerankEnabled(request.rerankEnabled())
                .maxContextTokens(request.maxContextTokens())
                .retrievedCount(candidates.size())
                .finalCount(rankedDocuments.size())
                .cacheHit(false)
                .retrievalLatencyMs(retrievalLatency)
                .rerankLatencyMs(rerankLatency)
                .totalLatencyMs(System.currentTimeMillis() - startTime)
                .traceId(traceId)
                .build();

        RagQuery savedQuery = queryRepository.save(query);

        UUID contextId = contextAssemblyService.saveContext(
                savedQuery.getId(), assembledContext, totalTokens, sources);

        if (ragProperties.getCache().getEnabled()) {
            semanticCacheService.cacheResult(
                    request.queryText(),
                    queryEmbedding,
                    contextId,
                    ragProperties.getCache().getTtlSeconds()
            );
        }

        publishQueryEvent(savedQuery);

        log.info("Completed retrieval for query: {} in {}ms", savedQuery.getId(), savedQuery.getTotalLatencyMs());

        return new RetrievalResponse(
                savedQuery.getId(),
                contextId,
                assembledContext,
                totalTokens,
                rankedDocuments.size(),
                sources,
                false,
                retrievalLatency,
                rerankLatency,
                savedQuery.getTotalLatencyMs(),
                traceId
        );
    }

    @Override
    public List<DocumentCandidate> searchVectorStore(String queryText, int topK, double similarityThreshold) {
        log.debug("Searching vector store with topK: {}, threshold: {}", topK, similarityThreshold);

        try {
            SearchRequest searchRequest = SearchRequest.query(queryText)
                    .withTopK(topK)
                    .withSimilarityThreshold(similarityThreshold);

            List<Document> documents = vectorStore.similaritySearch(searchRequest);

            return documents.stream()
                    .map(doc -> new DocumentCandidate(
                            UUID.fromString(doc.getId()),
                            UUID.randomUUID(),
                            doc.getContent(),
                            BigDecimal.valueOf(doc.getMetadata().getOrDefault("score", 0.0).toString()).setScale(4, java.math.RoundingMode.HALF_UP),
                            doc.getMetadata().toString()
                    ))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new VectorSearchException("Failed to search vector store", e);
        }
    }

    private String generateEmbedding(String text) {
        try {
            EmbeddingResponse response = embeddingModel.embedForResponse(List.of(text));
            return response.getResults().get(0).getOutput().toString();
        } catch (Exception e) {
            throw new VectorSearchException("Failed to generate embedding", e);
        }
    }

    private void publishQueryEvent(RagQuery query) {
        RagQueryEvent event = RagQueryEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType("RagQueryCreated")
                .correlationId(query.getTraceId())
                .payload(RagQueryEvent.RagQueryPayload.builder()
                        .queryId(query.getId())
                        .userId(query.getUserId())
                        .sessionId(query.getSessionId())
                        .queryText(query.getQueryText())
                        .topK(query.getTopK())
                        .rerankEnabled(query.getRerankEnabled())
                        .maxContextTokens(query.getMaxContextTokens())
                        .cacheHit(query.getCacheHit())
                        .totalLatencyMs(query.getTotalLatencyMs())
                        .traceId(query.getTraceId())
                        .build())
                .build();

        kafkaTemplate.send(RAG_QUERY_TOPIC, query.getId().toString(), event);
        log.debug("Published query event for query: {}", query.getId());
    }

    private String getTraceId() {
        if (tracer.currentSpan() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return UUID.randomUUID().toString();
    }
}
