package com.banking.vision.service.impl;

import com.banking.vision.config.VisionProperties;
import com.banking.vision.service.StorageService;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO storage service implementation.
 * 
 * Handles file storage operations in MinIO/S3:
 * - Upload files
 * - Download files
 * - Generate pre-signed URLs
 * - Delete files
 * 
 * Storage structure:
 * - originals/{userId}/{documentId}.{ext}
 * - processed/{userId}/{documentId}_preprocessed.png
 * - thumbnails/{userId}/{documentId}_thumb.jpg
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageServiceImpl implements StorageService {

    private final MinioClient minioClient;
    private final VisionProperties visionProperties;

    /**
     * Initialize MinIO bucket on startup.
     */
    @PostConstruct
    public void init() {
        String bucketName = visionProperties.getStorage().getBucketName();
        
        try {
            // Check if bucket exists
            boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build()
            );
            
            if (!exists) {
                log.info("Creating MinIO bucket: {}", bucketName);
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build()
                );
                log.info("MinIO bucket created successfully: {}", bucketName);
            } else {
                log.info("MinIO bucket already exists: {}", bucketName);
            }
            
        } catch (Exception e) {
            log.error("Failed to initialize MinIO bucket: {}", bucketName, e);
            throw new RuntimeException("MinIO initialization failed", e);
        }
    }

    @Override
    public String uploadFile(
        InputStream inputStream,
        String fileName,
        String contentType,
        UUID userId,
        UUID documentId
    ) {
        String extension = getFileExtension(fileName);
        String storageKey = buildOriginalStorageKey(userId, documentId, extension);
        
        log.info("Uploading file to MinIO: {}", storageKey);
        
        try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(visionProperties.getStorage().getBucketName())
                    .object(storageKey)
                    .stream(inputStream, -1, 10485760) // 10MB part size
                    .contentType(contentType)
                    .build()
            );
            
            log.info("File uploaded successfully: {}", storageKey);
            return storageKey;
            
        } catch (Exception e) {
            log.error("Failed to upload file to MinIO: {}", storageKey, e);
            throw new RuntimeException("File upload failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String uploadFile(File file, String contentType, UUID userId, UUID documentId) {
        log.info("Uploading file from File object: {}", file.getName());
        
        try (FileInputStream fis = new FileInputStream(file)) {
            return uploadFile(fis, file.getName(), contentType, userId, documentId);
        } catch (IOException e) {
            log.error("Failed to read file: {}", file.getName(), e);
            throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream downloadFile(String storageKey) {
        log.info("Downloading file from MinIO: {}", storageKey);
        
        try {
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(visionProperties.getStorage().getBucketName())
                    .object(storageKey)
                    .build()
            );
            
        } catch (Exception e) {
            log.error("Failed to download file from MinIO: {}", storageKey, e);
            throw new RuntimeException("File download failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void downloadFile(String storageKey, File destinationFile) {
        log.info("Downloading file to: {}", destinationFile.getAbsolutePath());
        
        try {
            minioClient.downloadObject(
                DownloadObjectArgs.builder()
                    .bucket(visionProperties.getStorage().getBucketName())
                    .object(storageKey)
                    .filename(destinationFile.getAbsolutePath())
                    .build()
            );
            
            log.info("File downloaded successfully to: {}", destinationFile.getAbsolutePath());
            
        } catch (Exception e) {
            log.error("Failed to download file to: {}", destinationFile.getAbsolutePath(), e);
            throw new RuntimeException("File download failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String generatePresignedUrl(String storageKey, int expirationSeconds) {
        log.debug("Generating pre-signed URL for: {}, expiration: {}s", storageKey, expirationSeconds);
        
        try {
            String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(visionProperties.getStorage().getBucketName())
                    .object(storageKey)
                    .expiry(expirationSeconds, TimeUnit.SECONDS)
                    .build()
            );
            
            log.debug("Pre-signed URL generated: {}", url);
            return url;
            
        } catch (Exception e) {
            log.error("Failed to generate pre-signed URL for: {}", storageKey, e);
            throw new RuntimeException("Failed to generate pre-signed URL: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String storageKey) {
        log.info("Deleting file from MinIO: {}", storageKey);
        
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(visionProperties.getStorage().getBucketName())
                    .object(storageKey)
                    .build()
            );
            
            log.info("File deleted successfully: {}", storageKey);
            
        } catch (Exception e) {
            log.error("Failed to delete file from MinIO: {}", storageKey, e);
            throw new RuntimeException("File deletion failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean fileExists(String storageKey) {
        log.debug("Checking if file exists: {}", storageKey);
        
        try {
            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(visionProperties.getStorage().getBucketName())
                    .object(storageKey)
                    .build()
            );
            return true;
            
        } catch (Exception e) {
            log.debug("File does not exist: {}", storageKey);
            return false;
        }
    }

    @Override
    public long getFileSize(String storageKey) {
        log.debug("Getting file size for: {}", storageKey);
        
        try {
            StatObjectResponse stat = minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(visionProperties.getStorage().getBucketName())
                    .object(storageKey)
                    .build()
            );
            
            return stat.size();
            
        } catch (Exception e) {
            log.error("Failed to get file size for: {}", storageKey, e);
            throw new RuntimeException("Failed to get file size: " + e.getMessage(), e);
        }
    }

    @Override
    public String buildOriginalStorageKey(UUID userId, UUID documentId, String extension) {
        return String.format("originals/%s/%s.%s", userId, documentId, extension);
    }

    @Override
    public String buildPreprocessedStorageKey(UUID userId, UUID documentId) {
        return String.format("processed/%s/%s_preprocessed.png", userId, documentId);
    }

    @Override
    public String buildThumbnailStorageKey(UUID userId, UUID documentId) {
        return String.format("thumbnails/%s/%s_thumb.jpg", userId, documentId);
    }

    /**
     * Extract file extension from filename.
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "bin";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
