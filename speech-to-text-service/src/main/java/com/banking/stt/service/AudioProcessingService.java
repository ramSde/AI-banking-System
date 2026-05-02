package com.banking.stt.service;

import com.banking.stt.domain.AudioFile;
import com.banking.stt.dto.AudioUploadRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

/**
 * Service interface for audio file processing operations.
 * Handles audio format conversion, validation, and storage.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
public interface AudioProcessingService {

    /**
     * Process and store uploaded audio file.
     * Validates, converts if necessary, and stores in object storage.
     *
     * @param file    Uploaded audio file
     * @param request Upload request parameters
     * @param userId  User ID
     * @return Saved AudioFile entity
     */
    AudioFile processAndStore(MultipartFile file, AudioUploadRequest request, UUID userId);

    /**
     * Convert audio file to Whisper-compatible format.
     * Converts to WAV format with 16kHz sample rate, mono channel.
     *
     * @param sourceFile Source audio file
     * @param targetFile Target file path
     * @return true if conversion successful
     */
    boolean convertAudioFormat(File sourceFile, File targetFile);

    /**
     * Get audio duration in seconds.
     *
     * @param file Audio file
     * @return Duration in seconds
     */
    Double getAudioDuration(File file);

    /**
     * Validate audio file format and size.
     *
     * @param file Audio file to validate
     * @return true if valid
     */
    boolean validateAudioFile(MultipartFile file);

    /**
     * Download audio file from storage.
     *
     * @param audioFileId Audio file ID
     * @return Downloaded file
     */
    File downloadAudioFile(UUID audioFileId);

    /**
     * Delete audio file from storage.
     *
     * @param storagePath Storage path
     */
    void deleteFromStorage(String storagePath);
}
