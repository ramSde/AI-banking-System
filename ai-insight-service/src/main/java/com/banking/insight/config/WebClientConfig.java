package com.banking.insight.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Value("${ai-orchestration.base-url}")
    private String aiOrchestrationBaseUrl;

    @Value("${transaction-service.base-url}")
    private String transactionServiceBaseUrl;

    @Value("${account-service.base-url}")
    private String accountServiceBaseUrl;

    @Bean
    public WebClient aiOrchestrationWebClient() {
        return WebClient.builder()
            .baseUrl(aiOrchestrationBaseUrl)
            .clientConnector(new ReactorClientHttpConnector(httpClient()))
            .build();
    }

    @Bean
    public WebClient transactionServiceWebClient() {
        return WebClient.builder()
            .baseUrl(transactionServiceBaseUrl)
            .clientConnector(new ReactorClientHttpConnector(httpClient()))
            .build();
    }

    @Bean
    public WebClient accountServiceWebClient() {
        return WebClient.builder()
            .baseUrl(accountServiceBaseUrl)
            .clientConnector(new ReactorClientHttpConnector(httpClient()))
            .build();
    }

    private HttpClient httpClient() {
        return HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
            .responseTimeout(Duration.ofSeconds(30))
            .doOnConnected(conn -> conn
                .addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS))
                .addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS))
            );
    }
}
