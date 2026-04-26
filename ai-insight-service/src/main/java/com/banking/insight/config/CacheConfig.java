package com.banking.insight.config;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class CacheConfig {

    @CacheEvict(value = {"insights", "patterns", "anomalies", "recommendations"}, allEntries = true)
    @Scheduled(fixedRateString = "${cache.eviction.rate:3600000}")
    public void evictAllCaches() {
    }
}
