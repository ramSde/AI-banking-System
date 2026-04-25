package com.banking.account.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA Configuration
 * 
 * Enables JPA auditing for automatic created_at/updated_at timestamps.
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.banking.account.repository")
@EnableTransactionManagement
public class JpaConfig {
}
