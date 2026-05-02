package com.banking.stt.mapper;

import com.banking.stt.domain.Transcription;
import com.banking.stt.domain.TranscriptionSegment;
import com.banking.stt.dto.TranscriptionResponse;
import com.banking.stt.dto.TranscriptionSegmentResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Transcription entities and DTOs.
 * Provides manual mapping methods for entity-DTO conversion.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Component
public class TranscriptionMapper {

    /**
     * Convert Transcription entity to TranscriptionResponse DTO.
     *
     * @param transcription Transcription entity
     * @return TranscriptionResponse DTO
     */
    public TranscriptionResponse toResponse(Transcription transcription) {
        if (transcription == null) {
            return null;
        }

        return TranscriptionResponse.builder()
                .id(transcription.getId())
                .audioFileId(transcription.getAudioFileId())
                .status(transcription.getStatus())
                .languageDetected(transcription.getLanguageDetected())
                .confidenceScore(transcription.getConfidenceScore())
                .fullText(transcription.getFullText())
                .wordCount(transcription.getWordCount())
                .processingTimeSeconds(transcription.getProcessingTimeSeconds())
                .modelUsed(transcription.getModelUsed())
                .errorMessage(transcription.getErrorMessage())
                .createdAt(transcription.getCreatedAt())
                .completedAt(transcription.isCompleted() ? transcription.getUpdatedAt() : null)
                .build();
    }

    /**
     * Convert TranscriptionSegment entity to TranscriptionSegmentResponse DTO.
     *
     * @param segment TranscriptionSegment entity
     * @return TranscriptionSegmentResponse DTO
     */
    public TranscriptionSegmentResponse toSegmentResponse(TranscriptionSegment segment) {
        if (segment == null) {
            return null;
        }

        return TranscriptionSegmentResponse.builder()
                .id(segment.getId())
                .segmentIndex(segment.getSegmentIndex())
                .startTimeSeconds(segment.getStartTimeSeconds())
                .endTimeSeconds(segment.getEndTimeSeconds())
                .durationSeconds(segment.getDurationSeconds())
                .text(segment.getText())
                .speakerId(segment.getSpeakerId())
                .confidenceScore(segment.getConfidenceScore())
                .wordCount(segment.getWordCount())
                .timeRange(segment.getFormattedTimeRange())
                .build();
    }
}
