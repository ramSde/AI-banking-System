package com.banking.rag.service;

import com.banking.rag.dto.DocumentCandidate;
import com.banking.rag.dto.RankedDocument;
import com.banking.rag.dto.RerankRequest;
import com.banking.rag.dto.RerankResponse;

import java.util.List;

public interface RerankingService {

    RerankResponse rerank(RerankRequest request);

    List<RankedDocument> rerankDocuments(String queryText, List<DocumentCandidate> documents, int topN);
}
