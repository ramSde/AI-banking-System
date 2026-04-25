package com.banking.rag.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.ChromaVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class SpringAiConfig {

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    @Value("${spring.ai.openai.base-url}")
    private String openAiBaseUrl;

    @Value("${spring.ai.vectorstore.chroma.client.host}")
    private String chromaHost;

    @Value("${spring.ai.vectorstore.chroma.client.port}")
    private int chromaPort;

    @Value("${spring.ai.vectorstore.chroma.collection-name}")
    private String collectionName;

    @Bean
    public OpenAiApi openAiApi() {
        return new OpenAiApi(openAiBaseUrl, openAiApiKey, RestClient.builder());
    }

    @Bean
    public EmbeddingModel embeddingModel(OpenAiApi openAiApi) {
        return new OpenAiEmbeddingModel(openAiApi);
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        String chromaUrl = String.format("http://%s:%d", chromaHost, chromaPort);
        return new ChromaVectorStore(embeddingModel, chromaUrl, collectionName, false);
    }
}
