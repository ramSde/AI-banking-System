package com.banking.fraud.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA Configuration
 * 
 * Enables JPA auditing and transaction management.
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.banking.fraud.repository")
@EnableTransactionManagement
public class JpaConfig {
}
