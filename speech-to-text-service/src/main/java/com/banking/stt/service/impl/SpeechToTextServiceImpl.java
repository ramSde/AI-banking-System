package com.banking.stt.service.impl;

import com.banking.stt.domain.*;
import com.banking.stt.dto.*;
import com.banking.stt.event.SttEventPublisher;
import com.banking.stt.exception.*;
import com.banking.stt.repository.*;
import com.banking.stt.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Main implementation of Speech-to-Text service.
 * Orchestrates audio upload, transcription, and export operations.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpeechToTextServiceImpl implements SpeechToTextService {

    private final AudioFileRepository audioFileRepository;
    private final TranscriptionRepository transcriptionRepository;
    private final TranscriptionSegmentRepository segmentRepository;
    private final AudioProcessingService audioProcessingService;
    private final TranscriptionService transcriptionService;
    private final ExportService exportService;
    private final SttEventPublisher eventPublisher;

    @Override
    @Transactional
    public AudioUploadResponse uploadAudio(MultipartFile file, AudioUploadRequest request, UUID userId) {
        log.info("Uploading audio file for user: {}", userId);

        // 1. Validate file
        validateAudioFile(file);

        // 2. Process and store audio
        AudioFile audioFile = audioProcessingService.processAndStore(file, request, userId);

        // 3. Create transcription record
        Transcription transcription = createTranscription(audioFile, request);

        // 4. Publish event
        eventPublisher.publishAudioUploaded(audioFile.getId(), userId);

        // 5. Trigger async transcription
        processTranscriptionAsync(transcription.getId());

        // 6. Build response
        return buildUploadResponse(audioFile, transcription);
    }

    @Override
    @Transactional(readOnly = true)
    public TranscriptionResponse getTranscription(UUID transcriptionId, UUID userId) {
        Transcription transcription = transcriptionRepository
                .findByIdAndUserIdAndDeletedAtIsNull(transcriptionId, userId)
                .orElseThrow(() -> new TranscriptionNotFoundException(transcriptionId));

        return buildTranscriptionResponse(transcription);
    }

    @Override
    @Transactional(readOnly = true)
    public TranscriptionStatusResponse getTranscriptionStatus(UUID transcriptionId, UUID userId) {
        Transcription transcription = transcriptionRepository
                .findByIdAndUserIdAndDeletedAtIsNull(transcriptionId, userId)
                .orElseThrow(() -> new TranscriptionNotFoundException(transcriptionId));

        return buildStatusResponse(transcription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TranscriptionSegmentResponse> getTranscriptionSegments(UUID transcriptionId, UUID userId) {
        // Verify ownership
        if (!transcriptionRepository.existsByIdAndUserIdAndDeletedAtIsNull(transcriptionId, userId)) {
            throw new TranscriptionNotFoundException(transcriptionId);
        }

        List<TranscriptionSegment> segments = segmentRepository
                .findByTranscriptionIdOrderBySegmentIndexAsc(transcriptionId);

        return segments.stream()
                .map(this::buildSegmentResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportTranscription(UUID transcriptionId, TranscriptionExportRequest request, UUID userId) {
        Transcription transcription = transcriptionRepository
                .findByIdAndUserIdAndDeletedAtIsNull(transcriptionId, userId)
                .orElseThrow(() -> new TranscriptionNotFoundException(transcriptionId));

        if (!transcription.isCompleted()) {
            throw new InvalidAudioFileException("Transcription is not completed yet");
        }

        List<TranscriptionSegment> segments = segmentRepository
                .findByTranscriptionIdOrderBySegmentIndexAsc(transcriptionId);

        return exportService.export(transcription, segments, request);
    }

    @Override
    @Transactional
    public void deleteAudio(UUID audioFileId, UUID userId) {
        AudioFile audioFile = audioFileRepository
                .findByIdAndDeletedAtIsNull(audioFileId)
                .orElseThrow(() -> new AudioFileNotFoundException(audioFileId));

        if (!audioFile.getUserId().equals(userId)) {
            throw new AudioFileNotFoundException(audioFileId);
        }

        // Soft delete audio file
        audioFile.softDelete();
        audioFileRepository.save(audioFile);

        // Soft delete associated transcriptions
        transcriptionRepository.findByAudioFileIdAndDeletedAtIsNull(audioFileId)
                .ifPresent(transcription -> {
                    transcription.softDelete();
                    transcriptionRepository.save(transcription);
                });

        // Delete from storage
        audioProcessingService.deleteFromStorage(audioFile.getStoragePath());

        log.info("Deleted audio file: {} for user: {}", audioFileId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TranscriptionResponse> listTranscriptions(UUID userId, int page, int size) {
        return transcriptionRepository
                .findByUserIdAndDeletedAtIsNull(userId, PageRequest.of(page, size))
                .map(this::buildTranscriptionResponse)
                .getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public AudioUploadResponse getAudioFile(UUID audioFileId, UUID userId) {
        AudioFile audioFile = audioFileRepository
                .findByIdAndDeletedAtIsNull(audioFileId)
                .orElseThrow(() -> new AudioFileNotFoundException(audioFileId));

        if (!audioFile.getUserId().equals(userId)) {
            throw new AudioFileNotFoundException(audioFileId);
        }

        Transcription transcription = transcriptionRepository
                .findByAudioFileIdAndDeletedAtIsNull(audioFileId)
                .orElse(null);

        return buildUploadResponse(audioFile, transcription);
    }

    @Async("transcriptionExecutor")
    protected void processTranscriptionAsync(UUID transcriptionId) {
        try {
            log.info("Starting async transcription: {}", transcriptionId);
            transcriptionService.transcribe(transcriptionId);
        } catch (Exception e) {
            log.error("Async transcription failed: {}", transcriptionId, e);
        }
    }

    private void validateAudioFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidAudioFileException("Audio file is empty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !isValidAudioFormat(filename)) {
            throw new UnsupportedAudioFormatException(getFileExtension(filename));
        }
    }

    private boolean isValidAudioFormat(String filename) {
        String extension = getFileExtension(filename);
        return AudioFormat.isSupported(extension);
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1) : "";
    }

    private Transcription createTranscription(AudioFile audioFile, AudioUploadRequest request) {
        Transcription transcription = Transcription.builder()
                .audioFileId(audioFile.getId())
                .userId(audioFile.getUserId())
                .status(TranscriptionStatus.PENDING)
                .languageDetected(request.getLanguageCode())
                .modelUsed("whisper-1")
                .build();

        return transcriptionRepository.save(transcription);
    }

    private AudioUploadResponse buildUploadResponse(AudioFile audioFile, Transcription transcription) {
        return AudioUploadResponse.builder()
                .audioFileId(audioFile.getId())
                .transcriptionId(transcription != null ? transcription.getId() : null)
                .filename(audioFile.getFilename())
                .fileSizeBytes(audioFile.getFileSizeBytes())
                .durationSeconds(audioFile.getDurationSeconds() != null ?
                        audioFile.getDurationSeconds().doubleValue() : null)
                .format(audioFile.getOriginalFormat().name())
                .languageCode(audioFile.getLanguageCode())
                .status(transcription != null ? transcription.getStatus() : null)
                .estimatedCompletionSeconds(estimateCompletionTime(audioFile))
                .uploadedAt(audioFile.getCreatedAt())
                .message("Audio uploaded successfully. Transcription in progress.")
                .build();
    }

    private Integer estimateCompletionTime(AudioFile audioFile) {
        // Rough estimate: 1 second of audio = 0.5 seconds processing time
        if (audioFile.getDurationSeconds() != null) {
            return audioFile.getDurationSeconds().divide(BigDecimal.valueOf(2), 0, BigDecimal.ROUND_UP).intValue();
        }
        return 30; // Default estimate
    }

    private TranscriptionResponse buildTranscriptionResponse(Transcription transcription) {
        AudioFile audioFile = audioFileRepository.findById(transcription.getAudioFileId()).orElse(null);
        
        int segmentCount = segmentRepository.countByTranscriptionId(transcription.getId());
        int speakerCount = segmentRepository.countDistinctSpeakersByTranscriptionId(transcription.getId());

        return TranscriptionResponse.builder()
                .id(transcription.getId())
                .audioFileId(transcription.getAudioFileId())
                .filename(audioFile != null ? audioFile.getFilename() : null)
                .status(transcription.getStatus())
                .languageDetected(transcription.getLanguageDetected())
                .confidenceScore(transcription.getConfidenceScore())
                .fullText(transcription.getFullText())
                .wordCount(transcription.getWordCount())
                .processingTimeSeconds(transcription.getProcessingTimeSeconds())
                .modelUsed(transcription.getModelUsed())
                .segmentCount(segmentCount)
                .speakerCount(speakerCount > 0 ? speakerCount : null)
                .errorMessage(transcription.getErrorMessage())
                .createdAt(transcription.getCreatedAt())
                .completedAt(transcription.isCompleted() ? transcription.getUpdatedAt() : null)
                .build();
    }

    private TranscriptionStatusResponse buildStatusResponse(Transcription transcription) {
        return TranscriptionStatusResponse.builder()
                .transcriptionId(transcription.getId())
                .status(transcription.getStatus())
                .progressPercentage(calculateProgress(transcription))
                .errorMessage(transcription.getErrorMessage())
                .startedAt(transcription.getCreatedAt())
                .completedAt(transcription.isCompleted() ? transcription.getUpdatedAt() : null)
                .estimatedTimeRemainingSeconds(estimateTimeRemaining(transcription))
                .message(getStatusMessage(transcription))
                .build();
    }

    private Integer calculateProgress(Transcription transcription) {
        return switch (transcription.getStatus()) {
            case PENDING -> 0;
            case PROCESSING -> 50;
            case COMPLETED -> 100;
            case FAILED -> 0;
        };
    }

    private Integer estimateTimeRemaining(Transcription transcription) {
        if (transcription.isCompleted() || transcription.isFailed()) {
            return 0;
        }
        // Simple estimation based on average processing time
        return 30; // Default 30 seconds
    }

    private String getStatusMessage(Transcription transcription) {
        return switch (transcription.getStatus()) {
            case PENDING -> "Transcription is queued for processing";
            case PROCESSING -> "Transcription is in progress";
            case COMPLETED -> "Transcription completed successfully";
            case FAILED -> "Transcription failed: " + transcription.getErrorMessage();
        };
    }

    private TranscriptionSegmentResponse buildSegmentResponse(TranscriptionSegment segment) {
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
