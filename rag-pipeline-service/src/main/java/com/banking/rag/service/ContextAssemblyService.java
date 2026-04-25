package com.banking.rag.service;

import com.banking.rag.domain.RagContext;
import com.banking.rag.domain.RagSource;

import java.util.List;
import java.util.UUID;

public interface ContextAssemblyService {

    /**
     * Assembles context from sources within token limit.
     * 
     * @param queryId Query ID
     * @param queryText Query text
     * @param sources List of sources
     * @param maxTokens Maximum token limit
     * @param includeSources Whether to include source attribution
     * @return Assembled context
     */
    RagContext assembleContext(
            UUID queryId,
            String queryText,
            List<RagSource> sources,
            int maxTokens,
            boolean includeSources
    );
}
