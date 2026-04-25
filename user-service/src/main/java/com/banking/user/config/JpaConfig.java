package com.banking.user.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA configuration for User Service.
 * Enables JPA auditing for automatic timestamp management.
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.banking.user.repository")
@EnableJpaAuditing
@EnableTransactionManagement
public class JpaConfig {
    // JPA configuration is handled by Spring Boot auto-configuration
    // This class explicitly enables JPA auditing and transaction management
}
