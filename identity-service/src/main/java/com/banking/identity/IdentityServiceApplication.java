package com.banking.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Identity Service Application
 * 
 * Production-grade identity and authentication service with:
 * - JWT access tokens (RS256, 15-minute TTL)
 * - Refresh token rotation (7-day TTL, bcrypt hashed)
 * - BCrypt password hashing (cost factor 12)
 * - Account lockout after failed attempts
 * - Kafka event publishing for audit trail
 * - Redis-backed token storage
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableKafka
public class IdentityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }
}
