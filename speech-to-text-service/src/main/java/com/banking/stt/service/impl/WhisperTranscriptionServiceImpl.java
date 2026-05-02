package com.banking.stt.service.impl;

import com.banking.stt.config.SttProperties;
import com.banking.stt.domain.*;
import com.banking.stt.event.SttEventPublisher;
import com.banking.stt.exception.AudioFileNotFoundException;
import com.banking.stt.exception.TranscriptionFailedException;
import com.banking.stt.repository.*;
import com.banking.stt.service.AudioProcessingService;
import com.banking.stt.service.TranscriptionService;
import com.theokanning.openai.audio.TranscriptionResult;
import com.theokanning.openai.service.OpenAiService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of transcription service using OpenAI Whisper API.
 * Handles speech-to-text conversion with retry and circuit breaker patterns.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WhisperTranscriptionServiceImpl implements TranscriptionService {

    private final TranscriptionRepository transcriptionRepository;
    private final AudioFileRepository audioFileRepository;
    private final TranscriptionSegmentRepository segmentRepository;
    private final AudioProcessingService audioProcessingService;
    private final SttEventPublisher eventPublisher;
    private final SttProperties sttProperties;

    @Override
    @Transactional
    @CircuitBreaker(name = "whisper-api", fallbackMethod = "transcribeFallback")
    @Retry(name = "whisper-api")
    public void transcribe(UUID transcriptionId) {
        log.info("Starting transcription: {}", transcriptionId);

        Transcription transcription = transcriptionRepository.findById(transcriptionId)
                .orElseThrow(() -> new TranscriptionFailedException("Transcription not found: " + transcriptionId));

        AudioFile audioFile = audioFileRepository.findById(transcription.getAudioFileId())
                .orElseThrow(() -> new AudioFileNotFoundException(transcription.getAudioFileId()));

        try {
            // 1. Mark as processing
            transcription.markAsProcessing();
            transcriptionRepository.save(transcription);

            long startTime = System.currentTimeMillis();

            // 2. Download audio file
            File audioFileObj = audioProcessingService.downloadAudioFile(audioFile.getId());

            // 3. Call Whisper API
            TranscriptionResult result = callWhisperAPI(audioFileObj, audioFile.getLanguageCode());

            long processingTime = System.currentTimeMillis() - startTime;

            // 4. Process result
            processTranscriptionResult(transcription, result, processingTime);

            // 5. Publish success event
            eventPublisher.publishTranscriptionCompleted(
                    transcription.getId(),
                    transcription.getUserId(),
                    transcription.getLanguageDetected(),
                    transcription.getWordCount()
            );

            log.info("Transcription completed successfully: {} in {}ms", transcriptionId, processingTime);

        } catch (Exception e) {
            log.error("Transcription failed: {}", transcriptionId, e);
            handleTranscriptionFailure(transcription, e);
            throw new TranscriptionFailedException("Transcription failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void retryTranscription(UUID transcriptionId) {
        log.info("Retrying transcription: {}", transcriptionId);

        Transcription transcription = transcriptionRepository.findById(transcriptionId)
                .orElseThrow(() -> new TranscriptionFailedException("Transcription not found"));

        if (!transcription.isFailed()) {
            throw new TranscriptionFailedException("Can only retry failed transcriptions");
        }

        // Reset status and retry
        transcription.setStatus(TranscriptionStatus.PENDING);
        transcription.setErrorMessage(null);
        transcriptionRepository.save(transcription);

        transcribe(transcriptionId);
    }

    @Override
    @Transactional
    public void cancelTranscription(UUID transcriptionId) {
        log.info("Cancelling transcription: {}", transcriptionId);

        Transcription transcription = transcriptionRepository.findById(transcriptionId)
                .orElseThrow(() -> new TranscriptionFailedException("Transcription not found"));

        if (transcription.isCompleted()) {
            throw new TranscriptionFailedException("Cannot cancel completed transcription");
        }

        transcription.markAsFailed("Cancelled by user");
        transcriptionRepository.save(transcription);
    }

    private TranscriptionResult callWhisperAPI(File audioFile, String languageCode) {
        log.info("Calling Whisper API for file: {}", audioFile.getName());

        try {
            // Initialize OpenAI service
            Duration timeout = Duration.ofSeconds(sttProperties.getWhisper().getTimeoutSeconds());
            OpenAiService openAiService = new OpenAiService(
                    sttProperties.getWhisper().getApiKey(),
                    timeout
            );

            // Build transcription request
            com.theokanning.openai.audio.TranscriptionRequest request =
                    com.theokanning.openai.audio.TranscriptionRequest.builder()
                            .model(sttProperties.getWhisper().getModel())
                            .language(languageCode)
                            .responseFormat("verbose_json")
                            .temperature(0.0)
                            .build();

            // Call Whisper API
            TranscriptionResult result = openAiService.createTranscription(request, audioFile);

            log.info("Whisper API call successful");
            return result;

        } catch (Exception e) {
            log.error("Whisper API call failed", e);
            throw new TranscriptionFailedException("Whisper API call failed: " + e.getMessage(), e);
        }
    }

    private void processTranscriptionResult(Transcription transcription,
                                            TranscriptionResult result,
                                            long processingTime) {
        log.info("Processing transcription result for: {}", transcription.getId());

        // Extract full text
        String fullText = result.getText();
        if (fullText == null || fullText.trim().isEmpty()) {
            throw new TranscriptionFailedException("Whisper API returned empty transcription");
        }

        // Count words
        int wordCount = countWords(fullText);

        // Update transcription
        transcription.markAsCompleted(fullText, wordCount, processingTime);
        transcription.setLanguageDetected(result.getLanguage());
        transcription.setConfidenceScore(BigDecimal.valueOf(95.0)); // Whisper doesn't provide overall confidence

        transcriptionRepository.save(transcription);

        // Create segments if available
        if (result.getSegments() != null && !result.getSegments().isEmpty()) {
            createSegments(transcription.getId(), result);
        } else {
            // Create single segment for entire transcription
            createSingleSegment(transcription);
        }

        log.info("Transcription result processed successfully");
    }

    private void createSegments(UUID transcriptionId, TranscriptionResult result) {
        log.info("Creating {} segments for transcription: {}", result.getSegments().size(), transcriptionId);

        List<TranscriptionSegment> segments = new ArrayList<>();

        for (int i = 0; i < result.getSegments().size(); i++) {
            var whisperSegment = result.getSegments().get(i);

            TranscriptionSegment segment = TranscriptionSegment.builder()
                    .transcriptionId(transcriptionId)
                    .segmentIndex(i)
                    .startTimeSeconds(BigDecimal.valueOf(whisperSegment.getStart()))
                    .endTimeSeconds(BigDecimal.valueOf(whisperSegment.getEnd()))
                    .text(whisperSegment.getText())
                    .confidenceScore(calculateSegmentConfidence(whisperSegment))
                    .build();

            segments.add(segment);
        }

        segmentRepository.saveAll(segments);
        log.info("Created {} segments", segments.size());
    }

    private void createSingleSegment(Transcription transcription) {
        log.info("Creating single segment for transcription: {}", transcription.getId());

        AudioFile audioFile = audioFileRepository.findById(transcription.getAudioFileId())
                .orElseThrow(() -> new AudioFileNotFoundException(transcription.getAudioFileId()));

        TranscriptionSegment segment = TranscriptionSegment.builder()
                .transcriptionId(transcription.getId())
                .segmentIndex(0)
                .startTimeSeconds(BigDecimal.ZERO)
                .endTimeSeconds(audioFile.getDurationSeconds() != null ?
                        audioFile.getDurationSeconds() : BigDecimal.ZERO)
                .text(transcription.getFullText())
                .confidenceScore(transcription.getConfidenceScore())
                .build();

        segmentRepository.save(segment);
        log.info("Created single segment");
    }

    private BigDecimal calculateSegmentConfidence(com.theokanning.openai.audio.TranscriptionSegment segment) {
        // Whisper API doesn't provide confidence scores in the current version
        // Return a default high confidence
        return BigDecimal.valueOf(95.0);
    }

    private int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }

    private void handleTranscriptionFailure(Transcription transcription, Exception e) {
        transcription.markAsFailed(e.getMessage());
        transcriptionRepository.save(transcription);

        // Publish failure event
        eventPublisher.publishTranscriptionFailed(
                transcription.getId(),
                transcription.getUserId(),
                e.getMessage()
        );
    }

    /**
     * Fallback method for circuit breaker.
     */
    private void transcribeFallback(UUID transcriptionId, Exception e) {
        log.error("Circuit breaker fallback triggered for transcription: {}", transcriptionId, e);

        Transcription transcription = transcriptionRepository.findById(transcriptionId)
                .orElse(null);

        if (transcription != null) {
            handleTranscriptionFailure(transcription, e);
        }

        throw new TranscriptionFailedException("Whisper API is currently unavailable. Please try again later.", e);
    }
}
