package com.banking.rag.service.impl;

import com.banking.rag.domain.RagSource;
import com.banking.rag.exception.RetrievalException;
import com.banking.rag.service.RetrievalService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class ChromaDbRetrievalService implements RetrievalService {

    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;

    public ChromaDbRetrievalService(VectorStore vectorStore, EmbeddingModel embeddingModel) {
        this.vectorStore = vectorStore;
        this.embeddingModel = embeddingModel;
    }

    @Override
    @CircuitBreaker(name = "chromadb", fallbackMethod = "retrieveFallback")
    @Retry(name = "chromadb")
    @TimeLimiter(name = "chromadb")
    public CompletableFuture<List<RagSource>> retrieve(String queryText, int topK, BigDecimal similarityThreshold) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Retrieving documents from ChromaDB for query: {}", queryText);

                SearchRequest searchRequest = SearchRequest.query(queryText)
                        .withTopK(topK)
                        .withSimilarityThreshold(similarityThreshold.doubleValue());

                List<Document> documents = vectorStore.similaritySearch(searchRequest);

                List<RagSource> sources = new ArrayList<>();
                int rank = 1;

                for (Document doc : documents) {
                    RagSource source = RagSource.builder()
                            .documentId(UUID.fromString(doc.getMetadata().getOrDefault("document_id", UUID.randomUUID().toString()).toString()))
                            .documentName(doc.getMetadata().getOrDefault("document_name", "Unknown").toString())
                            .chunkId(doc.getId())
                            .content(doc.getContent())
                            .similarityScore(BigDecimal.valueOf((Double) doc.getMetadata().getOrDefault("score", 0.0))
                                    .setScale(4, RoundingMode.HALF_UP))
                            .rank(rank++)
                            .metadata(doc.getMetadata())
                            .build();

                    sources.add(source);
                }

                log.info("Retrieved {} documents from ChromaDB", sources.size());
                return sources;

            } catch (Exception e) {
                log.error("Error retrieving documents from ChromaDB: {}", e.getMessage(), e);
                throw new RetrievalException("Failed to retrieve documents from vector store", e);
            }
        });
    }

    @Override
    @CircuitBreaker(name = "openai", fallbackMethod = "generateEmbeddingFallback")
    @Retry(name = "openai")
    @TimeLimiter(name = "openai")
    public CompletableFuture<float[]> generateEmbedding(String queryText) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("Generating embedding for query: {}", queryText);

                EmbeddingRequest request = new EmbeddingRequest(List.of(queryText), null);
                EmbeddingResponse response = embeddingModel.call(request);

                if (response.getResults().isEmpty()) {
                    throw new RetrievalException("No embedding generated for query");
                }

                float[] embedding = response.getResults().get(0).getOutput();
                log.debug("Generated embedding with dimension: {}", embedding.length);
                return embedding;

            } catch (Exception e) {
                log.error("Error generating embedding: {}", e.getMessage(), e);
                throw new RetrievalException("Failed to generate query embedding", e);
            }
        });
    }

    private CompletableFuture<List<RagSource>> retrieveFallback(String queryText, int topK, BigDecimal similarityThreshold, Exception e) {
        log.error("ChromaDB circuit breaker activated, returning empty results", e);
        return CompletableFuture.completedFuture(new ArrayList<>());
    }

    private CompletableFuture<float[]> generateEmbeddingFallback(String queryText, Exception e) {
        log.error("OpenAI circuit breaker activated, cannot generate embedding", e);
        throw new RetrievalException("Embedding generation service unavailable", e);
    }
}
