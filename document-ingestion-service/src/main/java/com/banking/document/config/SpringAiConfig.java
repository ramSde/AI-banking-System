package com.banking.document.config;

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

    private final String openAiApiKey;
    private final String chromaHost;
    private final int chromaPort;
    private final String chromaCollectionName;

    public SpringAiConfig(
            @Value("${spring.ai.openai.api-key}") String openAiApiKey,
            @Value("${spring.ai.vectorstore.chroma.client.host}") String chromaHost,
            @Value("${spring.ai.vectorstore.chroma.client.port}") int chromaPort,
            @Value("${spring.ai.vectorstore.chroma.collection-name}") String chromaCollectionName) {
        this.openAiApiKey = openAiApiKey;
        this.chromaHost = chromaHost;
        this.chromaPort = chromaPort;
        this.chromaCollectionName = chromaCollectionName;
    }

    @Bean
    public OpenAiApi openAiApi() {
        return new OpenAiApi(openAiApiKey);
    }

    @Bean
    public EmbeddingModel embeddingModel(OpenAiApi openAiApi) {
        return new OpenAiEmbeddingModel(openAiApi);
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        String chromaUrl = String.format("http://%s:%d", chromaHost, chromaPort);
        RestClient.Builder restClientBuilder = RestClient.builder().baseUrl(chromaUrl);
        
        return ChromaVectorStore.builder()
                .restClientBuilder(restClientBuilder)
                .embeddingModel(embeddingModel)
                .collectionName(chromaCollectionName)
                .initializeSchema(true)
                .build();
    }
}
