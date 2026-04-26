package com.banking.rag.service.impl;

import com.banking.rag.domain.RagContext;
import com.banking.rag.dto.DocumentSource;
import com.banking.rag.dto.RankedDocument;
import com.banking.rag.exception.ContextAssemblyException;
import com.banking.rag.repository.RagContextRepository;
import com.banking.rag.service.ContextAssemblyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContextAssemblyServiceImpl implements ContextAssemblyService {

    private final RagContextRepository contextRepository;
    private final ObjectMapper objectMapper;

    private static final int AVERAGE_TOKENS_PER_CHAR = 4;

    @Override
    public String assembleContext(List<RankedDocument> documents, int maxTokens) {
        log.debug("Assembling context from {} documents with max tokens: {}", documents.size(), maxTokens);

        StringBuilder context = new StringBuilder();
        int currentTokens = 0;
        int includedDocs = 0;

        for (RankedDocument doc : documents) {
            String docContent = formatDocument(doc, includedDocs + 1);
            int docTokens = countTokens(docContent);

            if (currentTokens + docTokens > maxTokens) {
                log.debug("Reached max tokens limit. Included {} documents", includedDocs);
                break;
            }

            context.append(docContent).append("\n\n");
            currentTokens += docTokens;
            includedDocs++;
        }

        if (includedDocs == 0) {
            throw new ContextAssemblyException("No documents could fit within the token limit");
        }

        log.info("Assembled context with {} documents and {} tokens", includedDocs, currentTokens);
        return context.toString().trim();
    }

    @Override
    public List<DocumentSource> extractSources(List<RankedDocument> documents) {
        return documents.stream()
                .map(doc -> new DocumentSource(
                        doc.documentId(),
                        doc.chunkId(),
                        null,
                        doc.content(),
                        null,
                        doc.initialScore(),
                        doc.rerankScore(),
                        null,
                        doc.metadata()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public int countTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return text.length() / AVERAGE_TOKENS_PER_CHAR;
    }

    @Override
    @Transactional
    public UUID saveContext(UUID queryId, String assembledContext, int totalTokens, List<DocumentSource> sources) {
        log.debug("Saving context for query: {}", queryId);

        try {
            String sourcesJson = objectMapper.writeValueAsString(sources);

            RagContext context = RagContext.builder()
                    .queryId(queryId)
                    .assembledContext(assembledContext)
                    .totalTokens(totalTokens)
                    .documentCount(sources.size())
                    .sources(sourcesJson)
                    .metadata(new HashMap<>())
                    .build();

            RagContext saved = contextRepository.save(context);
            log.info("Saved context with ID: {} for query: {}", saved.getId(), queryId);
            return saved.getId();

        } catch (JsonProcessingException e) {
            throw new ContextAssemblyException("Failed to serialize sources to JSON", e);
        }
    }

    private String formatDocument(RankedDocument doc, int position) {
        return String.format("[Document %d]\n%s", position, doc.content());
    }
}
