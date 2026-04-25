package com.banking.document.service;

import com.banking.document.domain.DocumentChunk;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface VectorStoreService {

    String storeVector(String text, List<Double> embedding, Map<String, Object> metadata);

    List<VectorSearchResult> searchSimilar(String query, int topK, double similarityThreshold);

    void deleteVector(String vectorId);

    void deleteVectorsByDocumentId(UUID documentId);

    record VectorSearchResult(
            String vectorId,
            String text,
            double similarityScore,
            Map<String, Object> metadata
    ) {}
}
