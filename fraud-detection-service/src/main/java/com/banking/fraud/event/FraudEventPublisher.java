package com.banking.fraud.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Fraud Event Publisher
 * 
 * Publishes fraud-related events to Kafka topics.
 */
@Component
public class FraudEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(FraudEventPublisher.class);

    private static final String ALERT_RAISED_TOPIC = "banking.fraud.alert-raised";
    private static final String TRANSACTION_BLOCKED_TOPIC = "banking.fraud.transaction-blocked";
    private static final String PATTERN_DETECTED_TOPIC = "banking.fraud.pattern-detected";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public FraudEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publish fraud alert raised event
     * 
     * @param event Fraud alert raised event
     */
    public void publishAlertRaised(FraudAlertRaisedEvent event) {
        try {
            kafkaTemplate.send(ALERT_RAISED_TOPIC, event.getCorrelationId(), event);
            log.info("Published FraudAlertRaisedEvent: alertId={}, transactionId={}", 
                    event.getPayload().getAlertId(), 
                    event.getPayload().getTransactionId());
        } catch (Exception e) {
            log.error("Failed to publish FraudAlertRaisedEvent: {}", e.getMessage(), e);
        }
    }

    /**
     * Publish transaction blocked event
     * 
     * @param event Transaction blocked event
     */
    public void publishTransactionBlocked(TransactionBlockedEvent event) {
        try {
            kafkaTemplate.send(TRANSACTION_BLOCKED_TOPIC, event.getCorrelationId(), event);
            log.info("Published TransactionBlockedEvent: transactionId={}, riskScore={}", 
                    event.getPayload().getTransactionId(), 
                    event.getPayload().getRiskScore());
        } catch (Exception e) {
            log.error("Failed to publish TransactionBlockedEvent: {}", e.getMessage(), e);
        }
    }

    /**
     * Publish fraud pattern detected event
     * 
     * @param event Fraud pattern detected event
     */
    public void publishPatternDetected(FraudPatternDetectedEvent event) {
        try {
            kafkaTemplate.send(PATTERN_DETECTED_TOPIC, event.getCorrelationId(), event);
            log.info("Published FraudPatternDetectedEvent: patternId={}, patternType={}", 
                    event.getPayload().getPatternId(), 
                    event.getPayload().getPatternType());
        } catch (Exception e) {
            log.error("Failed to publish FraudPatternDetectedEvent: {}", e.getMessage(), e);
        }
    }
}
