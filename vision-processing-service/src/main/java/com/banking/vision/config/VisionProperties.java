package com.banking.vision.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Configuration properties for Vision Processing Service.
 * 
 * Binds to application.yml properties with prefix "vision".
 */
@Data
@Validated
@ConfigurationProperties(prefix = "vision")
public class VisionProperties {

    /**
     * OCR configuration.
     */
    @NotNull
    private OcrConfig ocr = new OcrConfig();

    /**
     * Storage configuration.
     */
    @NotNull
    private StorageConfig storage = new StorageConfig();

    /**
     * Processing configuration.
     */
    @NotNull
    private ProcessingConfig processing = new ProcessingConfig();

    /**
     * File upload configuration.
     */
    @NotNull
    private UploadConfig upload = new UploadConfig();

    @Data
    public static class OcrConfig {
        /**
         * Path to Tesseract tessdata directory.
         */
        @NotBlank
        private String tessdataPath = "/usr/share/tesseract-ocr/4.00/tessdata";

        /**
         * Default OCR language.
         */
        @NotBlank
        private String defaultLanguage = "eng";

        /**
         * OCR processing timeout in seconds.
         */
        @Min(10)
        private int timeoutSeconds = 60;

        /**
         * Page segmentation mode (PSM).
         * 3 = Fully automatic page segmentation (default)
         */
        @Min(0)
        private int pageSegmentationMode = 3;

        /**
         * OCR engine mode (OEM).
         * 3 = Default, based on what is available
         */
        @Min(0)
        private int engineMode = 3;
    }

    @Data
    public static class StorageConfig {
        /**
         * MinIO/S3 endpoint URL.
         */
        @NotBlank
        private String endpoint;

        /**
         * Access key.
         */
        @NotBlank
        private String accessKey;

        /**
         * Secret key.
         */
        @NotBlank
        private String secretKey;

        /**
         * Bucket name for document storage.
         */
        @NotBlank
        private String bucketName = "banking-documents";

        /**
         * Pre-signed URL expiration in seconds.
         */
        @Min(60)
        private int urlExpirationSeconds = 3600;
    }

    @Data
    public static class ProcessingConfig {
        /**
         * File size threshold for async processing (in bytes).
         * Files larger than this will be processed asynchronously.
         */
        @Min(0)
        private long asyncThresholdBytes = 1048576; // 1MB

        /**
         * Maximum number of concurrent processing tasks.
         */
        @Min(1)
        private int maxConcurrentTasks = 10;

        /**
         * Enable image preprocessing (deskew, denoise, etc.).
         */
        private boolean enablePreprocessing = true;

        /**
         * Minimum confidence score to consider processing successful.
         */
        @Min(0)
        private double minConfidenceScore = 50.0;
    }

    @Data
    public static class UploadConfig {
        /**
         * Maximum file size in bytes.
         */
        @Min(1)
        private long maxFileSizeBytes = 10485760; // 10MB

        /**
         * Supported file formats (MIME types).
         */
        @NotNull
        private List<String> supportedFormats = List.of(
            "application/pdf",
            "image/png",
            "image/jpeg",
            "image/jpg",
            "image/tiff"
        );

        /**
         * Supported file extensions.
         */
        @NotNull
        private List<String> supportedExtensions = List.of(
            "pdf", "png", "jpg", "jpeg", "tiff", "tif"
        );
    }
}
