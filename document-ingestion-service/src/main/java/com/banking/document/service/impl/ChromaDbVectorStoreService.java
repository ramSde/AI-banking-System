package com.banking.document.service.impl;

import com.banking.document.service.EmbeddingService;
import com.banking.document.service.VectorStoreService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChromaDbVectorStoreService implements VectorStoreService {

    private final VectorStore vectorStore;
    private final EmbeddingService embeddingService;

    public ChromaDbVectorStoreService(VectorStore vectorStore, EmbeddingService embeddingService) {
        this.vectorStore = vectorStore;
        this.embeddingService = embeddingService;
    }

    @Override
    @CircuitBreaker(name = "vectorStoreService", fallbackMethod = "storeVectorFallback")
    @Retry(name = "vectorStoreService")
    public String storeVector(String text, List<Double> embedding, Map<String, Object> metadata) {
        String vectorId = UUID.randomUUID().toString();
        
        Document document = new Document(vectorId, text, metadata);
        vectorStore.add(List.of(document));
        
        log.debug("Stored vector with ID: {}", vectorId);
        return vectorId;
    }

    @Override
    @CircuitBreaker(name = "vectorStoreService", fallbackMethod = "searchSimilarFallback")
    @Retry(name = "vectorStoreService")
    public List<VectorSearchResult> searchSimilar(String query, int topK, double similarityThreshold) {
        log.debug("Searching for similar vectors - query length: {}, topK: {}, threshold: {}", 
                query.length(), topK, similarityThreshold);
        
        SearchRequest searchRequest = SearchRequest.query(query)
                .withTopK(topK)
                .withSimilarityThreshold(similarityThreshold);
        
        List<Document> results = vectorStore.similaritySearch(searchRequest);
        
        List<VectorSearchResult> searchResults = results.stream()
                .map(doc -> new VectorSearchResult(
                        doc.getId(),
                        doc.getContent(),
                        doc.getMetadata().getOrDefault("score", 0.0) instanceof Number 
                                ? ((Number) doc.getMetadata().get("score")).doubleValue() 
                                : 0.0,
                        doc.getMetadata()
                ))
                .collect(Collectors.toList());
        
        log.debug("Found {} similar vectors", searchResults.size());
        return searchResults;
    }

    @Override
    @CircuitBreaker(name = "vectorStoreService", fallbackMethod = "deleteVectorFallback")
    @Retry(name = "vectorStoreService")
    public void deleteVector(String vectorId) {
        vectorStore.delete(List.of(vectorId));
        log.debug("Deleted vector with ID: {}", vectorId);
    }

    @Override
    @CircuitBreaker(name = "vectorStoreService", fallbackMethod = "deleteVectorsByDocumentIdFallback")
    @Retry(name = "vectorStoreService")
    public void deleteVectorsByDocumentId(UUID documentId) {
        log.debug("Deleting all vectors for document ID: {}", documentId);
        
        SearchRequest searchRequest = SearchRequest.query("")
                .withTopK(1000)
                .withFilterExpression(String.format("document_id == '%s'", documentId.toString()));
        
        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        List<String> vectorIds = documents.stream()
                .map(Document::getId)
                .collect(Collectors.toList());
        
        if (!vectorIds.isEmpty()) {
            vectorStore.delete(vectorIds);
            log.info("Deleted {} vectors for document ID: {}", vectorIds.size(), documentId);
        }
    }

    private String storeVectorFallback(String text, List<Double> embedding, Map<String, Object> metadata, Exception ex) {
        log.error("Failed to store vector. Error: {}", ex.getMessage());
        throw new RuntimeException("Vector store service unavailable", ex);
    }

    private List<VectorSearchResult> searchSimilarFallback(String query, int topK, double similarityThreshold, Exception ex) {
        log.error("Failed to search similar vectors. Error: {}", ex.getMessage());
        throw new RuntimeException("Vector store service unavailable", ex);
    }

    private void deleteVectorFallback(String vectorId, Exception ex) {
        log.error("Failed to delete vector: {}. Error: {}", vectorId, ex.getMessage());
        throw new RuntimeException("Vector store service unavailable", ex);
    }

    private void deleteVectorsByDocumentIdFallback(UUID documentId, Exception ex) {
        log.error("Failed to delete vectors for document: {}. Error: {}", documentId, ex.getMessage());
        throw new RuntimeException("Vector store service unavailable", ex);
    }
}
