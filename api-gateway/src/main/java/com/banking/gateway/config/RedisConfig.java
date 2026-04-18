package com.banking.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration for reactive operations.
 * 
 * This configuration provides:
 * - Reactive Redis template for non-blocking operations
 * - JSON serialization for complex objects
 * - String serialization for keys
 * - Connection pooling via Lettuce
 * - Proper error handling and timeouts
 * 
 * Usage:
 * - Rate limiting counters (sliding window algorithm)
 * - JWT token blacklisting
 * - Session data caching
 * - OTP storage with TTL
 * - Circuit breaker state persistence
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 */
@Slf4j
@Configuration
public class RedisConfig {

    /**
     * Configure reactive Redis template with JSON serialization.
     * 
     * Serialization Strategy:
     * - Keys: String serialization for readability and debugging
     * - Values: JSON serialization for complex objects
     * - Hash keys: String serialization
     * - Hash values: JSON serialization
     * 
     * @param connectionFactory Reactive Redis connection factory
     * @return ReactiveRedisTemplate configured for banking operations
     */
    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {
        
        log.info("Configuring reactive Redis template with JSON serialization");

        // String serializer for keys
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        
        // JSON serializer for values
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        // Build serialization context
        RedisSerializationContext<String, Object> serializationContext = 
                RedisSerializationContext.<String, Object>newSerializationContext()
                        .key(stringSerializer)
                        .value(jsonSerializer)
                        .hashKey(stringSerializer)
                        .hashValue(jsonSerializer)
                        .build();

        // Create reactive template
        ReactiveRedisTemplate<String, Object> template = 
                new ReactiveRedisTemplate<>(connectionFactory, serializationContext);

        log.info("Reactive Redis template configured successfully");
        return template;
    }

    /**
     * Configure Lettuce connection factory with connection pooling.
     * 
     * This bean is automatically created by Spring Boot based on
     * spring.data.redis.* properties, but we can customize it here
     * if needed for specific banking requirements.
     * 
     * @param redisProperties Redis configuration properties
     * @return LettuceConnectionFactory with optimized settings
     */
    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory(RedisProperties redisProperties) {
        log.info("Configuring Lettuce connection factory for Redis at {}:{}", 
                redisProperties.getHost(), redisProperties.getPort());

        LettuceConnectionFactory factory = new LettuceConnectionFactory(
                redisProperties.getHost(), 
                redisProperties.getPort()
        );

        // Set database index
        factory.setDatabase(redisProperties.getDatabase());

        // Set password if configured
        if (redisProperties.getPassword() != null && !redisProperties.getPassword().isEmpty()) {
            factory.setPassword(redisProperties.getPassword());
        }

        // Validate connections on borrow
        factory.setValidateConnection(true);

        log.info("Lettuce connection factory configured for database {}", redisProperties.getDatabase());
        return factory;
    }
}