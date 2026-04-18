package com.banking.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration for reactive operations.
 * 
 * Configures Redis connection and serialization for:
 * - Rate limiting sliding window counters
 * - JWT token blacklist (logout/revocation)
 * - Session data caching
 * - Temporary data storage with TTL
 * 
 * Uses reactive Redis templates for non-blocking operations
 * compatible with Spring Cloud Gateway's reactive architecture.
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Configuration
public class RedisConfig {

    /**
     * Configures reactive Redis template with JSON serialization.
     * 
     * Key serialization: String (human-readable keys)
     * Value serialization: JSON (structured data support)
     * 
     * @param connectionFactory Reactive Redis connection factory
     * @return Configured ReactiveRedisTemplate
     */
    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {
        
        log.info("Configuring reactive Redis template with JSON serialization");
        
        // String serializer for keys (human-readable)
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        
        // JSON serializer for values (structured data)
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer();
        
        // Build serialization context
        RedisSerializationContext<String, Object> serializationContext = 
            RedisSerializationContext.<String, Object>newSerializationContext()
                .key(keySerializer)
                .value(valueSerializer)
                .hashKey(keySerializer)
                .hashValue(valueSerializer)
                .build();
        
        ReactiveRedisTemplate<String, Object> template = 
            new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
        
        log.info("Reactive Redis template configured successfully");
        return template;
    }

    /**
     * Configures reactive Redis template for string operations.
     * 
     * Optimized for simple string-based operations like counters
     * and flags where JSON serialization is unnecessary.
     * 
     * @param connectionFactory Reactive Redis connection factory
     * @return Configured ReactiveRedisTemplate for strings
     */
    @Bean
    public ReactiveRedisTemplate<String, String> reactiveStringRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {
        
        log.info("Configuring reactive Redis string template");
        
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        
        RedisSerializationContext<String, String> serializationContext = 
            RedisSerializationContext.<String, String>newSerializationContext()
                .key(stringSerializer)
                .value(stringSerializer)
                .hashKey(stringSerializer)
                .hashValue(stringSerializer)
                .build();
        
        ReactiveRedisTemplate<String, String> template = 
            new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
        
        log.info("Reactive Redis string template configured successfully");
        return template;
    }
}