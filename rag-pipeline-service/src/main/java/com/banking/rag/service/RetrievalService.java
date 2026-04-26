package com.banking.rag.service;

import com.banking.rag.dto.DocumentCandidate;
import com.banking.rag.dto.RetrievalRequest;
import com.banking.rag.dto.RetrievalResponse;

import java.util.List;
import java.util.UUID;

public interface RetrievalService {

    RetrievalResponse retrieve(RetrievalRequest request, UUID userId);

    List<DocumentCandidate> searchVectorStore(String queryText, int topK, double similarityThreshold);
}
