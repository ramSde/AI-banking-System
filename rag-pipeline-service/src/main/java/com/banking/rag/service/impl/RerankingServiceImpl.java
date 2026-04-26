package com.banking/rag.service.impl;

import com.banking.rag.config.RagProperties;
import com.banking.rag.dto.DocumentCandidate;
import com.banking.rag.dto.RankedDocument;
import com.banking.rag.dto.RerankRequest;
import com.banking.rag.dto.RerankResponse;
import com.banking.rag.exception.RerankingException;
import com.banking.rag.service.RerankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class RerankingServiceImpl implements RerankingService {

    private final RagProperties ragProperties;

    @Override
    public RerankResponse rerank(RerankRequest request) {
        log.debug("Reranking {} documents for query", request.documents().size());
        long startTime = System.currentTimeMillis();

        try {
            List<RankedDocument> rankedDocs = rerankDocuments(
                    request.queryText(),
                    request.documents(),
                    request.topN()
            );

            long latency = System.currentTimeMillis() - startTime;
            log.info("Reranked {} documents in {}ms", rankedDocs.size(), latency);

            return new RerankResponse(rankedDocs, latency);

        } catch (Exception e) {
            throw new RerankingException("Failed to rerank documents", e);
        }
    }

    @Override
    public List<RankedDocument> rerankDocuments(String queryText, List<DocumentCandidate> documents, int topN) {
        log.debug("Reranking {} documents with topN: {}", documents.size(), topN);

        List<RankedDocument> scoredDocuments = new ArrayList<>();

        for (int i = 0; i < documents.size(); i++) {
            DocumentCandidate doc = documents.get(i);
            BigDecimal rerankScore = calculateRerankScore(queryText, doc.content(), doc.initialScore());

            RankedDocument rankedDoc = new RankedDocument(
                    doc.documentId(),
                    doc.chunkId(),
                    doc.content(),
                    doc.initialScore(),
                    rerankScore,
                    i + 1,
                    doc.metadata()
            );
            scoredDocuments.add(rankedDoc);
        }

        List<RankedDocument> topDocuments = scoredDocuments.stream()
                .sorted(Comparator.comparing(RankedDocument::rerankScore).reversed())
                .limit(topN)
                .collect(Collectors.toList());

        List<RankedDocument> reranked = IntStream.range(0, topDocuments.size())
                .mapToObj(i -> new RankedDocument(
                        topDocuments.get(i).documentId(),
                        topDocuments.get(i).chunkId(),
                        topDocuments.get(i).content(),
                        topDocuments.get(i).initialScore(),
                        topDocuments.get(i).rerankScore(),
                        i + 1,
                        topDocuments.get(i).metadata()
                ))
                .collect(Collectors.toList());

        log.debug("Reranked to top {} documents", reranked.size());
        return reranked;
    }

    private BigDecimal calculateRerankScore(String query, String document, BigDecimal initialScore) {
        String queryLower = query.toLowerCase();
        String docLower = document.toLowerCase();

        int exactMatches = countExactMatches(queryLower, docLower);
        double matchBoost = exactMatches * 0.1;

        double finalScore = initialScore.doubleValue() + matchBoost;
        finalScore = Math.min(finalScore, 1.0);

        return BigDecimal.valueOf(finalScore).setScale(4, RoundingMode.HALF_UP);
    }

    private int countExactMatches(String query, String document) {
        String[] queryTerms = query.split("\\s+");
        int matches = 0;

        for (String term : queryTerms) {
            if (term.length() > 3 && document.contains(term)) {
                matches++;
            }
        }

        return matches;
    }
}
