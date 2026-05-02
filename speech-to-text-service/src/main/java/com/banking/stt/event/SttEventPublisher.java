package com.banking.stt.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Publisher for Speech-to-Text events to Kafka.
 * Publishes events for audio uploads, transcription completion, and failures.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SttEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String AUDIO_UPLOADED_TOPIC = "banking.stt.audio-uploaded";
    private static final String TRANSCRIPTION_COMPLETED_TOPIC = "banking.stt.transcription-completed";
    private static final String TRANSCRIPTION_FAILED_TOPIC = "banking.stt.transcription-failed";

    /**
     * Publish audio uploaded event.
     *
     * @param audioFileId Audio file ID
     * @param userId      User ID
     */
    public void publishAudioUploaded(UUID audioFileId, UUID userId) {
        log.info("Publishing audio uploaded event: audioFileId={}", audioFileId);

        AudioUploadedEvent event = AudioUploadedEvent.builder()
                .audioFileId(audioFileId)
                .userId(userId)
                .correlationId(UUID.randomUUID())
                .build();

        publishEvent(AUDIO_UPLOADED_TOPIC, audioFileId.toString(), event);
    }

    /**
     * Publish transcription completed event.
     *
     * @param transcriptionId   Transcription ID
     * @param userId            User ID
     * @param languageDetected  Detected language
     * @param wordCount         Word count
     */
    public void publishTranscriptionCompleted(UUID transcriptionId, UUID userId,
                                              String languageDetected, Integer wordCount) {
        log.info("Publishing transcription completed event: transcriptionId={}", transcriptionId);

        TranscriptionCompletedEvent event = TranscriptionCompletedEvent.builder()
                .transcriptionId(transcriptionId)
                .userId(userId)
                .languageDetected(languageDetected)
                .wordCount(wordCount)
                .correlationId(UUID.randomUUID())
                .build();

        publishEvent(TRANSCRIPTION_COMPLETED_TOPIC, transcriptionId.toString(), event);
    }

    /**
     * Publish transcription failed event.
     *
     * @param transcriptionId Transcription ID
     * @param userId          User ID
     * @param errorMessage    Error message
     */
    public void publishTranscriptionFailed(UUID transcriptionId, UUID userId, String errorMessage) {
        log.info("Publishing transcription failed event: transcriptionId={}", transcriptionId);

        TranscriptionFailedEvent event = TranscriptionFailedEvent.builder()
                .transcriptionId(transcriptionId)
                .userId(userId)
                .errorMessage(errorMessage)
                .correlationId(UUID.randomUUID())
                .build();

        publishEvent(TRANSCRIPTION_FAILED_TOPIC, transcriptionId.toString(), event);
    }

    private void publishEvent(String topic, String key, Object event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);

            CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send(topic, key, eventJson);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Event published successfully to topic: {}, partition: {}, offset: {}",
                            topic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish event to topic: {}", topic, ex);
                }
            });

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event", e);
        }
    }
}
