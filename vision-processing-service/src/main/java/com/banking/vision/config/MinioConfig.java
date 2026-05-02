package com.banking.vision.config;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO configuration for object storage.
 * 
 * MinIO is used for storing:
 * - Original uploaded documents
 * - Preprocessed images
 * - Thumbnails
 * 
 * Storage structure:
 * - banking-documents/originals/{userId}/{documentId}.{ext}
 * - banking-documents/processed/{userId}/{documentId}_preprocessed.png
 * - banking-documents/thumbnails/{userId}/{documentId}_thumb.jpg
 */
@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    private final VisionProperties visionProperties;

    /**
     * MinIO client bean.
     * 
     * Configured with endpoint, credentials from application properties.
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
            .endpoint(visionProperties.getStorage().getEndpoint())
            .credentials(
                visionProperties.getStorage().getAccessKey(),
                visionProperties.getStorage().getSecretKey()
            )
            .build();
    }
}
