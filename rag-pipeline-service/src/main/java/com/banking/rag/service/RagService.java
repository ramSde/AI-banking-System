package com.banking.rag.service;

import com.banking.rag.dto.RetrievalRequest;
import com.banking.rag.dto.RetrievalResponse;

import java.util.UUID;

public interface RagService {

    /**
     * Performs complete RAG pipeline: retrieval, reranking, and context assembly.
     * 
     * @param request Retrieval request with query and parameters
     * @return Retrieval response with assembled context and sources
     */
    RetrievalResponse retrieve(RetrievalRequest request);

    /**
     * Gets query details by ID.
     * 
     * @param queryId Query ID
     * @return Retrieval response
     */
    RetrievalResponse getQueryById(UUID queryId);
}
