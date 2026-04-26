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
public class VectorStoreConfig {

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    @Value("${spring.ai.vectorstore.chroma.client.host}")
    private String chromaHost;

    @Value("${spring.ai.vectorstore.chroma.client.port}")
    private int chromaPort;

    @Value("${spring.ai.vectorstore.chroma.collection-name}")
    private String collectionName;

    @Bean
    public EmbeddingModel embeddingModel() {
        return new OpenAiEmbeddingModel(new OpenAiApi(openAiApiKey));
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        String chromaUrl = String.format("http://%s:%d", chromaHost, chromaPort);
        RestClient.Builder restClientBuilder = RestClient.builder().baseUrl(chromaUrl);
        
        return ChromaVectorStore.builder()
                .restClientBuilder(restClientBuilder)
                .embeddingModel(embeddingModel)
                .collectionName(collectionName)
                .initializeSchema(false)
                .build();
    }
}
