package com.banking.document.service.impl;

import com.banking.document.service.EmbeddingService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SpringAiEmbeddingService implements EmbeddingService {

    private final EmbeddingModel embeddingModel;

    public SpringAiEmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @Override
    @CircuitBreaker(name = "embeddingService", fallbackMethod = "generateEmbeddingFallback")
    @Retry(name = "embeddingService")
    public List<Double> generateEmbedding(String text) {
        log.debug("Generating embedding for text of length: {}", text.length());
        
        EmbeddingRequest request = new EmbeddingRequest(List.of(text), null);
        EmbeddingResponse response = embeddingModel.call(request);
        
        List<Double> embedding = response.getResults().get(0).getOutput();
        log.debug("Generated embedding with dimension: {}", embedding.size());
        
        return embedding;
    }

    @Override
    @CircuitBreaker(name = "embeddingService", fallbackMethod = "generateEmbeddingsFallback")
    @Retry(name = "embeddingService")
    public List<List<Double>> generateEmbeddings(List<String> texts) {
        log.debug("Generating embeddings for {} texts", texts.size());
        
        EmbeddingRequest request = new EmbeddingRequest(texts, null);
        EmbeddingResponse response = embeddingModel.call(request);
        
        List<List<Double>> embeddings = response.getResults().stream()
                .map(result -> result.getOutput())
                .collect(Collectors.toList());
        
        log.debug("Generated {} embeddings", embeddings.size());
        
        return embeddings;
    }

    private List<Double> generateEmbeddingFallback(String text, Exception ex) {
        log.error("Failed to generate embedding, returning empty list. Error: {}", ex.getMessage());
        throw new RuntimeException("Embedding service unavailable", ex);
    }

    private List<List<Double>> generateEmbeddingsFallback(List<String> texts, Exception ex) {
        log.error("Failed to generate embeddings, returning empty list. Error: {}", ex.getMessage());
        throw new RuntimeException("Embedding service unavailable", ex);
    }
}
