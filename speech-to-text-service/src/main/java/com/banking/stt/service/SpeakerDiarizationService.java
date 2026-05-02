package com.banking.stt.service;

import com.banking.stt.domain.TranscriptionSegment;
import com.banking.stt.dto.SpeakerInfoResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for speaker diarization.
 * Identifies and separates different speakers in audio.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
public interface SpeakerDiarizationService {

    /**
     * Perform speaker diarization on transcription segments.
     * Assigns speaker IDs to segments based on voice characteristics.
     *
     * @param transcriptionId Transcription ID
     * @param segments        List of transcription segments
     * @param expectedSpeakers Expected number of speakers (optional)
     * @return List of segments with speaker IDs assigned
     */
    List<TranscriptionSegment> performDiarization(
            UUID transcriptionId,
            List<TranscriptionSegment> segments,
            Integer expectedSpeakers
    );

    /**
     * Get speaker information for transcription.
     *
     * @param transcriptionId Transcription ID
     * @return List of speaker information
     */
    List<SpeakerInfoResponse> getSpeakerInfo(UUID transcriptionId);

    /**
     * Count unique speakers in transcription.
     *
     * @param transcriptionId Transcription ID
     * @return Number of unique speakers
     */
    int countSpeakers(UUID transcriptionId);
}
