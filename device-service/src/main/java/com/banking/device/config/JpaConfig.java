package com.banking.device.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA configuration for device service.
 * Enables JPA auditing and transaction management.
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.banking.device.repository")
@EnableTransactionManagement
public class JpaConfig {
}