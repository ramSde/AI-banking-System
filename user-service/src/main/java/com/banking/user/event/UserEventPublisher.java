package com.banking.user.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publisher for user-related Kafka events.
 * Publishes events to appropriate topics.
 */
@Component
@Slf4j
public class UserEventPublisher {

    private static final String USER_CREATED_TOPIC = "banking.user.user-created";
    private static final String USER_UPDATED_TOPIC = "banking.user.user-updated";
    private static final String KYC_STATUS_CHANGED_TOPIC = "banking.user.kyc-status-changed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public UserEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publish user created event
     */
    public void publishUserCreated(UserCreatedEvent event) {
        log.info("Publishing UserCreatedEvent for user: {}", event.payload().userId());
        kafkaTemplate.send(USER_CREATED_TOPIC, event.payload().userId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish UserCreatedEvent: {}", ex.getMessage());
                    } else {
                        log.debug("UserCreatedEvent published successfully");
                    }
                });
    }

    /**
     * Publish user updated event
     */
    public void publishUserUpdated(UserUpdatedEvent event) {
        log.info("Publishing UserUpdatedEvent for user: {}", event.payload().userId());
        kafkaTemplate.send(USER_UPDATED_TOPIC, event.payload().userId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish UserUpdatedEvent: {}", ex.getMessage());
                    } else {
                        log.debug("UserUpdatedEvent published successfully");
                    }
                });
    }

    /**
     * Publish KYC status changed event
     */
    public void publishKycStatusChanged(KycStatusChangedEvent event) {
        log.info("Publishing KycStatusChangedEvent for user: {}", event.payload().userId());
        kafkaTemplate.send(KYC_STATUS_CHANGED_TOPIC, event.payload().userId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish KycStatusChangedEvent: {}", ex.getMessage());
                    } else {
                        log.debug("KycStatusChangedEvent published successfully");
                    }
                });
    }
}
