package com.banking.rag.service;

import com.banking.rag.domain.RagSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface RetrievalService {

    /**
     * Retrieves relevant documents from vector store based on query.
     * 
     * @param queryText Query text
     * @param topK Number of documents to retrieve
     * @param similarityThreshold Minimum similarity score
     * @return CompletableFuture with list of retrieved sources
     */
    CompletableFuture<List<RagSource>> retrieve(String queryText, int topK, BigDecimal similarityThreshold);

    /**
     * Generates embedding for query text.
     * 
     * @param queryText Query text
     * @return CompletableFuture with embedding vector
     */
    CompletableFuture<float[]> generateEmbedding(String queryText);
}
