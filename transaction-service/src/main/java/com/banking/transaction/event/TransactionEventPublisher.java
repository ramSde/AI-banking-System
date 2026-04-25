package com.banking.transaction.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Transaction Event Publisher
 * 
 * Publishes transaction domain events to Kafka topics.
 * All publishing is asynchronous to avoid blocking transaction processing.
 */
@Component
public class TransactionEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventPublisher.class);

    private static final String TRANSACTION_CREATED_TOPIC = "banking.transaction.transaction-created";
    private static final String TRANSACTION_COMPLETED_TOPIC = "banking.transaction.transaction-completed";
    private static final String TRANSACTION_FAILED_TOPIC = "banking.transaction.transaction-failed";
    private static final String TRANSACTION_REVERSED_TOPIC = "banking.transaction.transaction-reversed";
    private static final String HOLD_CREATED_TOPIC = "banking.transaction.hold-created";
    private static final String HOLD_RELEASED_TOPIC = "banking.transaction.hold-released";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TransactionEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Async
    public void publishTransactionCreated(TransactionCreatedEvent event) {
        try {
            kafkaTemplate.send(TRANSACTION_CREATED_TOPIC, event.correlationId(), event);
            log.info("Published TransactionCreatedEvent: {}", event.correlationId());
        } catch (Exception e) {
            log.error("Failed to publish TransactionCreatedEvent: {}", event.correlationId(), e);
        }
    }

    @Async
    public void publishTransactionCompleted(TransactionCompletedEvent event) {
        try {
            kafkaTemplate.send(TRANSACTION_COMPLETED_TOPIC, event.correlationId(), event);
            log.info("Published TransactionCompletedEvent: {}", event.correlationId());
        } catch (Exception e) {
            log.error("Failed to publish TransactionCompletedEvent: {}", event.correlationId(), e);
        }
    }

    @Async
    public void publishTransactionFailed(TransactionFailedEvent event) {
        try {
            kafkaTemplate.send(TRANSACTION_FAILED_TOPIC, event.correlationId(), event);
            log.info("Published TransactionFailedEvent: {}", event.correlationId());
        } catch (Exception e) {
            log.error("Failed to publish TransactionFailedEvent: {}", event.correlationId(), e);
        }
    }

    @Async
    public void publishTransactionReversed(TransactionReversedEvent event) {
        try {
            kafkaTemplate.send(TRANSACTION_REVERSED_TOPIC, event.correlationId(), event);
            log.info("Published TransactionReversedEvent: {}", event.correlationId());
        } catch (Exception e) {
            log.error("Failed to publish TransactionReversedEvent: {}", event.correlationId(), e);
        }
    }

    @Async
    public void publishHoldCreated(HoldCreatedEvent event) {
        try {
            kafkaTemplate.send(HOLD_CREATED_TOPIC, event.correlationId(), event);
            log.info("Published HoldCreatedEvent: {}", event.correlationId());
        } catch (Exception e) {
            log.error("Failed to publish HoldCreatedEvent: {}", event.correlationId(), e);
        }
    }

    @Async
    public void publishHoldReleased(HoldReleasedEvent event) {
        try {
            kafkaTemplate.send(HOLD_RELEASED_TOPIC, event.correlationId(), event);
            log.info("Published HoldReleasedEvent: {}", event.correlationId());
        } catch (Exception e) {
            log.error("Failed to publish HoldReleasedEvent: {}", event.correlationId(), e);
        }
    }
}
