package com.banking.vision.consumer;

import com.banking.vision.event.DocumentUploadedEvent;
import com.banking.vision.service.VisionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for document upload events.
 * Listens to document upload events and triggers asynchronous OCR processing.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentUploadConsumer {

    private final VisionService visionService;
    private final ObjectMapper objectMapper;

    /**
     * Consume document uploaded events.
     * Triggers OCR processing for uploaded documents.
     *
     * @param event          Document uploaded event
     * @param partition      Kafka partition
     * @param offset         Kafka offset
     * @param acknowledgment Kafka acknowledgment
     */
    @KafkaListener(
            topics = "${kafka.topics.document-uploaded:document-uploaded}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeDocumentUploadedEvent(
            @Payload String event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received document uploaded event from partition {} at offset {}", partition, offset);

        try {
            // Deserialize event
            DocumentUploadedEvent uploadedEvent = objectMapper.readValue(event, DocumentUploadedEvent.class);

            log.info("Processing document upload event for document ID: {}", uploadedEvent.getDocumentId());

            // Trigger OCR processing
            processDocumentUpload(uploadedEvent);

            // Acknowledge message
            acknowledgment.acknowledge();

            log.info("Successfully processed document upload event for document ID: {}",
                    uploadedEvent.getDocumentId());

        } catch (Exception e) {
            log.error("Error processing document uploaded event from partition {} at offset {}",
                    partition, offset, e);

            // In production, implement retry logic or send to DLQ
            // For now, acknowledge to prevent reprocessing
            acknowledgment.acknowledge();
        }
    }

    /**
     * Process document upload event.
     * This method can be extended to implement additional processing logic.
     *
     * @param event Document uploaded event
     */
    private void processDocumentUpload(DocumentUploadedEvent event) {
        try {
            log.debug("Starting OCR processing for document: {}", event.getDocumentId());

            // The actual OCR processing is already triggered in VisionServiceImpl.uploadDocument()
            // This consumer can be used for additional async processing if needed:
            // - Send notifications
            // - Update external systems
            // - Trigger downstream workflows
            // - Generate thumbnails
            // - Extract metadata

            // For now, just log the event
            log.info("Document {} uploaded by user {} at {}",
                    event.getDocumentId(),
                    event.getUserId(),
                    event.getTimestamp());

            // Example: You could trigger additional processing here
            // visionService.performAdditionalProcessing(event.getDocumentId());

        } catch (Exception e) {
            log.error("Error in document upload processing for document: {}",
                    event.getDocumentId(), e);
            throw e;
        }
    }

    /**
     * Consume processing completed events (optional).
     * Can be used to trigger downstream workflows.
     *
     * @param event          Processing completed event
     * @param partition      Kafka partition
     * @param offset         Kafka offset
     * @param acknowledgment Kafka acknowledgment
     */
    @KafkaListener(
            topics = "${kafka.topics.processing-completed:processing-completed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeProcessingCompletedEvent(
            @Payload String event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received processing completed event from partition {} at offset {}", partition, offset);

        try {
            log.debug("Processing completed event: {}", event);

            // Implement downstream processing logic here:
            // - Send success notifications
            // - Update dashboards
            // - Trigger analytics
            // - Archive documents

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing completed event from partition {} at offset {}",
                    partition, offset, e);
            acknowledgment.acknowledge();
        }
    }

    /**
     * Consume processing failed events (optional).
     * Can be used for error handling and alerting.
     *
     * @param event          Processing failed event
     * @param partition      Kafka partition
     * @param offset         Kafka offset
     * @param acknowledgment Kafka acknowledgment
     */
    @KafkaListener(
            topics = "${kafka.topics.processing-failed:processing-failed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeProcessingFailedEvent(
            @Payload String event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.warn("Received processing failed event from partition {} at offset {}", partition, offset);

        try {
            log.error("Processing failed event: {}", event);

            // Implement error handling logic here:
            // - Send failure notifications
            // - Create support tickets
            // - Trigger retry mechanisms
            // - Update monitoring dashboards

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error handling processing failed event from partition {} at offset {}",
                    partition, offset, e);
            acknowledgment.acknowledge();
        }
    }
}
