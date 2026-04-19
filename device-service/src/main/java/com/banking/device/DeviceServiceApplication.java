package com.banking.device;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Device Intelligence Service Application.
 * Provides device fingerprinting, trust scoring, and anomaly detection.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableKafka
@EnableAsync
public class DeviceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeviceServiceApplication.class, args);
    }
}
