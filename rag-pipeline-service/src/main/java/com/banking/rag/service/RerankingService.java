package com.banking.rag.service;

import com.banking.rag.domain.RagSource;

import java.util.List;

public interface RerankingService {

    /**
     * Reranks retrieved sources using cross-encoder model.
     * 
     * @param queryText Query text
     * @param sources List of sources to rerank
     * @param topN Number of top results to return
     * @return Reranked list of sources
     */
    List<RagSource> rerank(String queryText, List<RagSource> sources, int topN);
}
