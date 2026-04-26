package com.banking.rag.service;

import com.banking.rag.dto.DocumentSource;
import com.banking.rag.dto.RankedDocument;

import java.util.List;
import java.util.UUID;

public interface ContextAssemblyService {

    String assembleContext(List<RankedDocument> documents, int maxTokens);

    List<DocumentSource> extractSources(List<RankedDocument> documents);

    int countTokens(String text);

    UUID saveContext(UUID queryId, String assembledContext, int totalTokens, List<DocumentSource> sources);
}
