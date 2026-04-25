package com.banking.rag.service.impl;

import com.banking.rag.domain.RagContext;
import com.banking.rag.domain.RagSource;
import com.banking.rag.exception.ContextAssemblyException;
import com.banking.rag.repository.RagContextRepository;
import com.banking.rag.service.ContextAssemblyService;
import com.banking.rag.util.TokenCounter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class TokenAwareContextAssemblyService implements ContextAssemblyService {

    private final RagContextRepository ragContextRepository;
    private final TokenCounter tokenCounter;

    public TokenAwareContextAssemblyService(
            RagContextRepository ragContextRepository,
            TokenCounter tokenCounter) {
        this.ragContextRepository = ragContextRepository;
        this.tokenCounter = tokenCounter;
    }

    @Override
    @Transactional
    public RagContext assembleContext(
            UUID queryId,
            String queryText,
            List<RagSource> sources,
            int maxTokens,
            boolean includeSources) {

        try {
            log.info("Assembling context for query {} with max tokens: {}", queryId, maxTokens);

            StringBuilder contextBuilder = new StringBuilder();
            List<RagSource> includedSources = new ArrayList<>();
            int currentTokens = 0;

            int reservedTokens = includeSources ? 200 : 50;
            int availableTokens = maxTokens - reservedTokens;

            for (RagSource source : sources) {
                String content = source.getContent();
                int contentTokens = tokenCounter.countTokens(content);

                if (currentTokens + contentTokens <= availableTokens) {
                    if (contextBuilder.length() > 0) {
                        contextBuilder.append("\n\n");
                    }
                    contextBuilder.append(content);
                    includedSources.add(source);
                    currentTokens += contentTokens;
                } else {
                    int remainingTokens = availableTokens - currentTokens;
                    if (remainingTokens > 100) {
                        String truncatedContent = tokenCounter.truncateToTokenLimit(content, remainingTokens);
                        if (contextBuilder.length() > 0) {
                            contextBuilder.append("\n\n");
                        }
                        contextBuilder.append(truncatedContent);
                        
                        RagSource truncatedSource = RagSource.builder()
                                .documentId(source.getDocumentId())
                                .documentName(source.getDocumentName())
                                .chunkId(source.getChunkId())
                                .content(truncatedContent)
                                .similarityScore(source.getSimilarityScore())
                                .rerankScore(source.getRerankScore())
                                .rank(source.getRank())
                                .metadata(source.getMetadata())
                                .build();
                        includedSources.add(truncatedSource);
                        currentTokens += remainingTokens;
                    }
                    break;
                }
            }

            String assembledContext = contextBuilder.toString();
            int finalTokenCount = tokenCounter.countTokens(assembledContext);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("maxTokens", maxTokens);
            metadata.put("requestedSources", sources.size());
            metadata.put("includedSources", includedSources.size());

            RagContext ragContext = RagContext.builder()
                    .queryId(queryId)
                    .assembledContext(assembledContext)
                    .tokenCount(finalTokenCount)
                    .sourceCount(includedSources.size())
                    .sources(includeSources ? includedSources : null)
                    .metadata(metadata)
                    .build();

            RagContext savedContext = ragContextRepository.save(ragContext);

            log.info("Assembled context with {} tokens from {} sources", finalTokenCount, includedSources.size());
            return savedContext;

        } catch (Exception e) {
            log.error("Error assembling context: {}", e.getMessage(), e);
            throw new ContextAssemblyException("Failed to assemble context", e);
        }
    }
}
