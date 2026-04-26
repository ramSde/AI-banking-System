package com.banking.chat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${ai.orchestration.url}")
    private String aiOrchestrationUrl;

    @Value("${ai.orchestration.timeout.connect:5000}")
    private int aiOrchestrationConnectTimeout;

    @Value("${ai.orchestration.timeout.read:60000}")
    private int aiOrchestrationReadTimeout;

    @Value("${ai.rag.url}")
    private String ragPipelineUrl;

    @Value("${ai.rag.timeout.connect:5000}")
    private int ragConnectTimeout;

    @Value("${ai.rag.timeout.read:30000}")
    private int ragReadTimeout;

    @Bean
    public RestClient aiOrchestrationClient() {
        return RestClient.builder()
                .baseUrl(aiOrchestrationUrl)
                .requestFactory(createRequestFactory(aiOrchestrationConnectTimeout, aiOrchestrationReadTimeout))
                .build();
    }

    @Bean
    public RestClient ragPipelineClient() {
        return RestClient.builder()
                .baseUrl(ragPipelineUrl)
                .requestFactory(createRequestFactory(ragConnectTimeout, ragReadTimeout))
                .build();
    }

    private ClientHttpRequestFactory createRequestFactory(int connectTimeout, int readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        return factory;
    }
}
