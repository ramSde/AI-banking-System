package com.banking.rag.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
@ConfigurationProperties(prefix = "rag")
@Data
public class RagProperties {

    private Retrieval retrieval = new Retrieval();
    private Reranking reranking = new Reranking();
    private Context context = new Context();
    private Cache cache = new Cache();

    @Data
    public static class Retrieval {
        private Integer topK = 10;
        private BigDecimal similarityThreshold = new BigDecimal("0.7");
        private Integer maxResults = 20;
    }

    @Data
    public static class Reranking {
        private Boolean enabled = true;
        private String model = "cross-encoder/ms-marco-MiniLM-L-6-v2";
        private Integer topN = 5;
    }

    @Data
    public static class Context {
        private Integer maxTokens = 4000;
        private Integer chunkOverlap = 200;
    }

    @Data
    public static class Cache {
        private Boolean enabled = true;
        private Integer ttlSeconds = 3600;
        private BigDecimal similarityThreshold = new BigDecimal("0.95");
        private Integer maxEntries = 10000;
    }
}
