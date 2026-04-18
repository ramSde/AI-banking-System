package com.banking.risk.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA configuration for the risk service.
 * Enables JPA auditing and transaction management.
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.banking.risk.repository")
@EnableTransactionManagement
public class JpaConfig {
}
