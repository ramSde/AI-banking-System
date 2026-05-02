package com.banking.vision.event;

import com.banking.vision.domain.VisionDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Publisher for vision processing events.
 * 
 * Publishes events to Kafka topics:
 * - banking.vision.document-uploaded
 * - banking.vision.processing-completed
 * - banking.vision.processing-failed
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VisionEventPublisher {

    private static final String TOPIC_DOCUMENT_UPLOADED = "banking.vision.document-uploaded";
    private static final String TOPIC_PROCESSING_COMPLETED = "banking.vision.processing-completed";
    private static final String TOPIC_PROCESSING_FAILED = "banking.vision.processing-failed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publish document uploaded event.
     */
    public void publishDocumentUploaded(VisionDocument document) {
        try {
            DocumentUploadedEvent event = DocumentUploadedEvent.builder()
                .correlationId(document.getId().toString())
                .payload(DocumentUploadedEvent.Payload.builder()
                    .documentId(document.getId())
                    .userId(document.getUserId())
                    .documentType(document.getDocumentType())
                    .filename(document.getOriginalFilename())
                    .fileSize(document.getFileSize())
                    .mimeType(document.getMimeType())
                    .storageKey(document.getStorageKey())
                    .build())
                .build();

            kafkaTemplate.send(TOPIC_DOCUMENT_UPLOADED, document.getId().toString(), event);
            log.info("Published DocumentUploadedEvent for document: {}", document.getId());
            
        } catch (Exception e) {
            log.error("Failed to publish DocumentUploadedEvent: {}", e.getMessage(), e);
        }
    }

    /**
     * Publish processing completed event.
     */
    public void publishProcessingCompleted(
        VisionDocument document,
        Long processingTimeMs,
        Integer pageCount,
        Map<String, Object> extractedData
    ) {
        try {
            ProcessingCompletedEvent event = ProcessingCompletedEvent.builder()
                .correlationId(document.getId().toString())
                .payload(ProcessingCompletedEvent.Payload.builder()
                    .documentId(document.getId())
                    .userId(document.getUserId())
                    .documentType(document.getDocumentType())
                    .confidenceScore(document.getConfidenceScore())
                    .processingTimeMs(processingTimeMs)
                    .pageCount(pageCount)
                    .extractedData(extractedData)
                    .build())
                .build();

            kafkaTemplate.send(TOPIC_PROCESSING_COMPLETED, document.getId().toString(), event);
            log.info("Published ProcessingCompletedEvent for document: {}", document.getId());
            
        } catch (Exception e) {
            log.error("Failed to publish ProcessingCompletedEvent: {}", e.getMessage(), e);
        }
    }

    /**
     * Publish processing failed event.
     */
    public void publishProcessingFailed(VisionDocument document, String errorMessage) {
        try {
            ProcessingFailedEvent event = ProcessingFailedEvent.builder()
                .correlationId(document.getId().toString())
                .payload(ProcessingFailedEvent.Payload.builder()
                    .documentId(document.getId())
                    .userId(document.getUserId())
                    .documentType(document.getDocumentType())
                    .errorMessage(errorMessage)
                    .errorCode("PROCESSING_FAILED")
                    .build())
                .build();

            kafkaTemplate.send(TOPIC_PROCESSING_FAILED, document.getId().toString(), event);
            log.info("Published ProcessingFailedEvent for document: {}", document.getId());
            
        } catch (Exception e) {
            log.error("Failed to publish ProcessingFailedEvent: {}", e.getMessage(), e);
        }
    }
}
