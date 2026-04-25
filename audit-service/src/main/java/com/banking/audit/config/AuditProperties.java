package com.banking.audit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "audit")
@Data
public class AuditProperties {

    private int retentionDays = 2555;
    private int queryMaxResults = 1000;
    private boolean partitionEnabled = true;
    private int partitionIntervalMonths = 3;
}
