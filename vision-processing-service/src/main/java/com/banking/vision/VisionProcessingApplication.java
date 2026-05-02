package com.banking.vision;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Vision Processing Service - Main Application
 * 
 * Provides OCR and document intelligence capabilities for:
 * - Receipts
 * - Invoices
 * - Bank statements
 * - Checks
 * - ID documents
 * 
 * Technology Stack:
 * - Tesseract OCR 5.x for text extraction
 * - Apache PDFBox for PDF processing
 * - MinIO/S3 for document storage
 * - Redis for caching
 * - Kafka for async event processing
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableKafka
@EnableAsync
@ConfigurationPropertiesScan
public class VisionProcessingApplication {

    public static void main(String[] args) {
        SpringApplication.run(VisionProcessingApplication.class, args);
    }
}
