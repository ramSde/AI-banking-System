package com.banking.stt.service;

import com.banking.stt.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Main service interface for Speech-to-Text operations.
 * Orchestrates audio upload, transcription, and export functionality.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
public interface SpeechToTextService {

    /**
     * Upload audio file and initiate transcription.
     *
     * @param file   Audio file to upload
     * @param request Upload request parameters
     * @param userId User ID
     * @return Upload response with transcription ID
     */
    AudioUploadResponse uploadAudio(MultipartFile file, AudioUploadRequest request, UUID userId);

    /**
     * Get transcription by ID.
     *
     * @param transcriptionId Transcription ID
     * @param userId          User ID
     * @return Transcription response
     */
    TranscriptionResponse getTranscription(UUID transcriptionId, UUID userId);

    /**
     * Get transcription status.
     *
     * @param transcriptionId Transcription ID
     * @param userId          User ID
     * @return Transcription status response
     */
    TranscriptionStatusResponse getTranscriptionStatus(UUID transcriptionId, UUID userId);

    /**
     * Get transcription segments with timestamps.
     *
     * @param transcriptionId Transcription ID
     * @param userId          User ID
     * @return List of transcription segments
     */
    List<TranscriptionSegmentResponse> getTranscriptionSegments(UUID transcriptionId, UUID userId);

    /**
     * Export transcription in specified format.
     *
     * @param transcriptionId Transcription ID
     * @param request         Export request with format
     * @param userId          User ID
     * @return Exported file as byte array
     */
    byte[] exportTranscription(UUID transcriptionId, TranscriptionExportRequest request, UUID userId);

    /**
     * Delete audio file and associated transcription.
     *
     * @param audioFileId Audio file ID
     * @param userId      User ID
     */
    void deleteAudio(UUID audioFileId, UUID userId);

    /**
     * List transcriptions for user.
     *
     * @param userId User ID
     * @param page   Page number
     * @param size   Page size
     * @return List of transcriptions
     */
    List<TranscriptionResponse> listTranscriptions(UUID userId, int page, int size);

    /**
     * Get audio file details.
     *
     * @param audioFileId Audio file ID
     * @param userId      User ID
     * @return Audio upload response
     */
    AudioUploadResponse getAudioFile(UUID audioFileId, UUID userId);
}
