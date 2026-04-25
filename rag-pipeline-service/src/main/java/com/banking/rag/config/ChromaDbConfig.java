package com.banking.rag.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChromaDbConfig {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final TimeLimiterRegistry timeLimiterRegistry;

    public ChromaDbConfig(
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry,
            TimeLimiterRegistry timeLimiterRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
        this.timeLimiterRegistry = timeLimiterRegistry;
    }

    @Bean
    public CircuitBreaker chromaDbCircuitBreaker() {
        return circuitBreakerRegistry.circuitBreaker("chromadb");
    }

    @Bean
    public Retry chromaDbRetry() {
        return retryRegistry.retry("chromadb");
    }

    @Bean
    public TimeLimiter chromaDbTimeLimiter() {
        return timeLimiterRegistry.timeLimiter("chromadb");
    }

    @Bean
    public CircuitBreaker openAiCircuitBreaker() {
        return circuitBreakerRegistry.circuitBreaker("openai");
    }

    @Bean
    public Retry openAiRetry() {
        return retryRegistry.retry("openai");
    }

    @Bean
    public TimeLimiter openAiTimeLimiter() {
        return timeLimiterRegistry.timeLimiter("openai");
    }
}
