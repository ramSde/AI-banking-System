package com.banking.document.event;

import com.banking.document.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DocumentEventConsumer {

    private final DocumentService documentService;

    public DocumentEventConsumer(DocumentService documentService) {
        this.documentService = documentService;
    }

    @KafkaListener(
            topics = "${document.kafka.topics.upload-requested}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleDocumentUploadRequested(
            @Payload DocumentUploadRequestedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received DocumentUploadRequestedEvent - documentId: {}, userId: {}, topic: {}, partition: {}, offset: {}",
                event.getPayload().getDocumentId(),
                event.getPayload().getUserId(),
                topic,
                partition,
                offset);

        try {
            documentService.processDocument(event.getPayload().getDocumentId());
            acknowledgment.acknowledge();
            log.info("Successfully processed DocumentUploadRequestedEvent - documentId: {}", 
                    event.getPayload().getDocumentId());
        } catch (Exception e) {
            log.error("Error processing DocumentUploadRequestedEvent - documentId: {}, error: {}",
                    event.getPayload().getDocumentId(),
                    e.getMessage(),
                    e);
            acknowledgment.acknowledge();
        }
    }
}
