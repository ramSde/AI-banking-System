package com.banking.account.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Account Event Publisher
 * 
 * Publishes account-related events to Kafka topics.
 */
@Component
public class AccountEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AccountEventPublisher.class);
    
    private static final String ACCOUNT_CREATED_TOPIC = "banking.account.account-created";
    private static final String ACCOUNT_UPDATED_TOPIC = "banking.account.account-updated";
    private static final String ACCOUNT_STATUS_CHANGED_TOPIC = "banking.account.status-changed";
    private static final String ACCOUNT_CLOSED_TOPIC = "banking.account.account-closed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public AccountEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishAccountCreated(AccountCreatedEvent event) {
        try {
            kafkaTemplate.send(ACCOUNT_CREATED_TOPIC, event.getPayload().getAccountId().toString(), event);
            log.info("Published AccountCreatedEvent for account: {}", event.getPayload().getAccountNumber());
        } catch (Exception e) {
            log.error("Failed to publish AccountCreatedEvent: {}", e.getMessage(), e);
        }
    }

    public void publishAccountUpdated(AccountUpdatedEvent event) {
        try {
            kafkaTemplate.send(ACCOUNT_UPDATED_TOPIC, event.getPayload().getAccountId().toString(), event);
            log.info("Published AccountUpdatedEvent for account: {}", event.getPayload().getAccountNumber());
        } catch (Exception e) {
            log.error("Failed to publish AccountUpdatedEvent: {}", e.getMessage(), e);
        }
    }

    public void publishAccountStatusChanged(AccountStatusChangedEvent event) {
        try {
            kafkaTemplate.send(ACCOUNT_STATUS_CHANGED_TOPIC, event.getPayload().getAccountId().toString(), event);
            log.info("Published AccountStatusChangedEvent for account: {} - Status: {} -> {}", 
                    event.getPayload().getAccountNumber(),
                    event.getPayload().getPreviousStatus(),
                    event.getPayload().getNewStatus());
        } catch (Exception e) {
            log.error("Failed to publish AccountStatusChangedEvent: {}", e.getMessage(), e);
        }
    }

    public void publishAccountClosed(AccountClosedEvent event) {
        try {
            kafkaTemplate.send(ACCOUNT_CLOSED_TOPIC, event.getPayload().getAccountId().toString(), event);
            log.info("Published AccountClosedEvent for account: {}", event.getPayload().getAccountNumber());
        } catch (Exception e) {
            log.error("Failed to publish AccountClosedEvent: {}", e.getMessage(), e);
        }
    }
}
