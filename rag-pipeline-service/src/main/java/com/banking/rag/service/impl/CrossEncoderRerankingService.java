package com.banking.rag.service.impl;

import com.banking.rag.domain.RagSource;
import com.banking.rag.exception.RerankingException;
import com.banking.rag.service.RerankingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CrossEncoderRerankingService implements RerankingService {

    @Override
    public List<RagSource> rerank(String queryText, List<RagSource> sources, int topN) {
        try {
            log.info("Reranking {} sources for query", sources.size());

            List<RagSource> rerankedSources = sources.stream()
                    .map(source -> {
                        BigDecimal rerankScore = calculateRerankScore(queryText, source.getContent());
                        source.setRerankScore(rerankScore);
                        return source;
                    })
                    .sorted(Comparator.comparing(RagSource::getRerankScore).reversed())
                    .limit(topN)
                    .collect(Collectors.toList());

            int rank = 1;
            for (RagSource source : rerankedSources) {
                source.setRank(rank++);
            }

            log.info("Reranked to top {} sources", rerankedSources.size());
            return rerankedSources;

        } catch (Exception e) {
            log.error("Error during reranking: {}", e.getMessage(), e);
            throw new RerankingException("Failed to rerank sources", e);
        }
    }

    private BigDecimal calculateRerankScore(String query, String content) {
        String queryLower = query.toLowerCase();
        String contentLower = content.toLowerCase();

        int matchCount = 0;
        String[] queryTokens = queryLower.split("\\s+");

        for (String token : queryTokens) {
            if (contentLower.contains(token)) {
                matchCount++;
            }
        }

        double score = (double) matchCount / queryTokens.length;
        
        int contentLength = content.length();
        double lengthPenalty = Math.min(1.0, contentLength / 1000.0);
        
        double finalScore = (score * 0.7) + (lengthPenalty * 0.3);

        return BigDecimal.valueOf(finalScore)
                .setScale(4, RoundingMode.HALF_UP);
    }
}
