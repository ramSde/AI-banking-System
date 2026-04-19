package com.banking.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * User Service Application - Profile Management, Preferences, KYC Status
 * 
 * This service manages user profiles with encrypted PII, user preferences,
 * and KYC verification status for regulatory compliance.
 * 
 * Features:
 * - User profile management with PII encryption
 * - User preferences (language, timezone, notifications)
 * - KYC document management and status tracking
 * - Redis caching for performance
 * - Kafka event-driven architecture
 * - OpenAPI documentation
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableKafka
@EnableAsync
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
