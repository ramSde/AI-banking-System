package com.banking.transaction.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA Configuration
 * 
 * Enables JPA auditing for automatic created_at/updated_at timestamps
 * and transaction management for declarative @Transactional support.
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.banking.transaction.repository")
@EnableTransactionManagement
public class JpaConfig {
}
