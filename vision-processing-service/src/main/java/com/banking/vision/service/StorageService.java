package com.banking.vision.service;

import java.io.File;
import java.io.InputStream;
import java.util.UUID;

/**
 * Service interface for object storage operations.
 * 
 * Handles file storage in MinIO/S3:
 * - Upload original documents
 * - Store preprocessed images
 * - Generate pre-signed URLs
 * - Delete files
 */
public interface StorageService {

    /**
     * Upload file to storage.
     * 
     * @param inputStream File input stream
     * @param fileName File name
     * @param contentType MIME type
     * @param userId User ID
     * @param documentId Document ID
     * @return Storage key
     */
    String uploadFile(
        InputStream inputStream,
        String fileName,
        String contentType,
        UUID userId,
        UUID documentId
    );

    /**
     * Upload file from File object.
     * 
     * @param file File object
     * @param contentType MIME type
     * @param userId User ID
     * @param documentId Document ID
     * @return Storage key
     */
    String uploadFile(
        File file,
        String contentType,
        UUID userId,
        UUID documentId
    );

    /**
     * Download file from storage.
     * 
     * @param storageKey Storage key
     * @return File input stream
     */
    InputStream downloadFile(String storageKey);

    /**
     * Download file to local file.
     * 
     * @param storageKey Storage key
     * @param destinationFile Destination file
     */
    void downloadFile(String storageKey, File destinationFile);

    /**
     * Generate pre-signed URL for file download.
     * 
     * @param storageKey Storage key
     * @param expirationSeconds URL expiration in seconds
     * @return Pre-signed URL
     */
    String generatePresignedUrl(String storageKey, int expirationSeconds);

    /**
     * Delete file from storage.
     * 
     * @param storageKey Storage key
     */
    void deleteFile(String storageKey);

    /**
     * Check if file exists.
     * 
     * @param storageKey Storage key
     * @return True if exists, false otherwise
     */
    boolean fileExists(String storageKey);

    /**
     * Get file size.
     * 
     * @param storageKey Storage key
     * @return File size in bytes
     */
    long getFileSize(String storageKey);

    /**
     * Build storage key for original document.
     * 
     * @param userId User ID
     * @param documentId Document ID
     * @param extension File extension
     * @return Storage key
     */
    String buildOriginalStorageKey(UUID userId, UUID documentId, String extension);

    /**
     * Build storage key for preprocessed image.
     * 
     * @param userId User ID
     * @param documentId Document ID
     * @return Storage key
     */
    String buildPreprocessedStorageKey(UUID userId, UUID documentId);

    /**
     * Build storage key for thumbnail.
     * 
     * @param userId User ID
     * @param documentId Document ID
     * @return Storage key
     */
    String buildThumbnailStorageKey(UUID userId, UUID documentId);
}
