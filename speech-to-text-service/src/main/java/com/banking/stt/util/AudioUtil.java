package com.banking.stt.util;

import com.banking.stt.domain.AudioFormat;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

/**
 * Utility class for audio file operations.
 * Provides helper methods for audio file validation and manipulation.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Slf4j
public final class AudioUtil {

    private AudioUtil() {
        // Utility class
    }

    /**
     * Get file extension from filename.
     *
     * @param filename Filename
     * @return File extension (without dot)
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }

        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1).toLowerCase();
        }

        return "";
    }

    /**
     * Check if file extension is supported.
     *
     * @param filename Filename
     * @return true if supported
     */
    public static boolean isSupportedFormat(String filename) {
        String extension = getFileExtension(filename);
        return AudioFormat.isSupported(extension);
    }

    /**
     * Format file size in human-readable format.
     *
     * @param bytes File size in bytes
     * @return Formatted string (e.g., "2.5 MB")
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * Format duration in human-readable format.
     *
     * @param seconds Duration in seconds
     * @return Formatted string (e.g., "2:30" or "1:05:30")
     */
    public static String formatDuration(double seconds) {
        int totalSeconds = (int) seconds;
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int secs = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%d:%02d", minutes, secs);
        }
    }

    /**
     * Convert audio file to Base64 string.
     *
     * @param file Audio file
     * @return Base64 encoded string
     * @throws IOException if file cannot be read
     */
    public static String encodeToBase64(File file) throws IOException {
        byte[] fileContent = Files.readAllBytes(file.toPath());
        return Base64.getEncoder().encodeToString(fileContent);
    }

    /**
     * Decode Base64 string to byte array.
     *
     * @param base64 Base64 encoded string
     * @return Decoded byte array
     */
    public static byte[] decodeFromBase64(String base64) {
        return Base64.getDecoder().decode(base64);
    }

    /**
     * Sanitize filename by removing special characters.
     *
     * @param filename Original filename
     * @return Sanitized filename
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "audio";
        }

        // Remove path separators and special characters
        String sanitized = filename.replaceAll("[/\\\\:*?\"<>|]", "_");

        // Limit length
        if (sanitized.length() > 255) {
            String extension = getFileExtension(sanitized);
            int maxNameLength = 255 - extension.length() - 1;
            sanitized = sanitized.substring(0, maxNameLength) + "." + extension;
        }

        return sanitized;
    }

    /**
     * Check if file is within size limit.
     *
     * @param fileSizeBytes File size in bytes
     * @param maxSizeMB     Maximum size in MB
     * @return true if within limit
     */
    public static boolean isWithinSizeLimit(long fileSizeBytes, int maxSizeMB) {
        long maxSizeBytes = (long) maxSizeMB * 1024 * 1024;
        return fileSizeBytes <= maxSizeBytes;
    }

    /**
     * Check if duration is within limit.
     *
     * @param durationSeconds Duration in seconds
     * @param maxMinutes      Maximum duration in minutes
     * @return true if within limit
     */
    public static boolean isWithinDurationLimit(double durationSeconds, int maxMinutes) {
        double maxSeconds = maxMinutes * 60.0;
        return durationSeconds <= maxSeconds;
    }
}
