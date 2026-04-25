package com.banking.fraud.service.impl;

import com.banking.fraud.service.FraudDetectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Transaction Event Consumer
 * 
 * Consumes transaction events from Kafka for fraud detection.
 */
@Service
public class TransactionEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventConsumer.class);

    private final FraudDetectionService fraudDetectionService;

    public TransactionEventConsumer(FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
    }

    /**
     * Consume transaction created events
     * 
     * @param event Transaction event
     * @param partition Kafka partition
     * @param offset Kafka offset
     * @param acknowledgment Kafka acknowledgment
     */
    @KafkaListener(
            topics = "banking.transaction.transaction-created",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeTransactionCreated(
            @Payload Map<String, Object> event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        try {
            log.info("Received transaction-created event: partition={}, offset={}", partition, offset);
            
            Map<String, Object> payload = extractPayload(event);
            fraudDetectionService.processTransactionEvent(payload);
            
            acknowledgment.acknowledge();
            log.debug("Acknowledged transaction-created event: partition={}, offset={}", partition, offset);
        } catch (Exception e) {
            log.error("Failed to process transaction-created event: partition={}, offset={}, error={}",
                    partition, offset, e.getMessage(), e);
            acknowledgment.acknowledge();
        }
    }

    /**
     * Consume transaction completed events
     * 
     * @param event Transaction event
     * @param partition Kafka partition
     * @param offset Kafka offset
     * @param acknowledgment Kafka acknowledgment
     */
    @KafkaListener(
            topics = "banking.transaction.transaction-completed",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeTransactionCompleted(
            @Payload Map<String, Object> event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        try {
            log.info("Received transaction-completed event: partition={}, offset={}", partition, offset);
            
            Map<String, Object> payload = extractPayload(event);
            fraudDetectionService.processTransactionEvent(payload);
            
            acknowledgment.acknowledge();
            log.debug("Acknowledged transaction-completed event: partition={}, offset={}", partition, offset);
        } catch (Exception e) {
            log.error("Failed to process transaction-completed event: partition={}, offset={}, error={}",
                    partition, offset, e.getMessage(), e);
            acknowledgment.acknowledge();
        }
    }

    /**
     * Extract payload from event
     * 
     * @param event Event map
     * @return Payload map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractPayload(Map<String, Object> event) {
        if (event.containsKey("payload")) {
            return (Map<String, Object>) event.get("payload");
        }
        return event;
    }
}
