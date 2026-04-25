package com.banking.audit.event;

import com.banking.audit.domain.AuditEvent;
import com.banking.audit.domain.EntityType;
import com.banking.audit.domain.EventType;
import com.banking.audit.service.AuditService;
import com.banking.audit.util.JsonDiffCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class AuditEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(AuditEventConsumer.class);
    private final AuditService auditService;
    private final JsonDiffCalculator jsonDiffCalculator;

    public AuditEventConsumer(AuditService auditService, JsonDiffCalculator jsonDiffCalculator) {
        this.auditService = auditService;
        this.jsonDiffCalculator = jsonDiffCalculator;
    }

    @KafkaListener(
            topicPattern = "banking\\..*\\.audit-event",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeAuditEvent(
            @Payload Map<String, Object> eventPayload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_PARTITION, required = false) Integer partition,
            @Header(value = KafkaHeaders.OFFSET, required = false) Long offset,
            Acknowledgment acknowledgment
    ) {
        try {
            logger.info("Received audit event from topic: {}, partition: {}, offset: {}", topic, partition, offset);
            logger.debug("Event payload: {}", eventPayload);

            AuditEvent auditEvent = mapToAuditEvent(eventPayload);
            auditService.saveAuditEvent(auditEvent);

            acknowledgment.acknowledge();
            logger.info("Successfully processed and acknowledged audit event: eventId={}", auditEvent.getEventId());

        } catch (Exception e) {
            logger.error("Error processing audit event from topic: {}, partition: {}, offset: {}, error: {}",
                    topic, partition, offset, e.getMessage(), e);
            acknowledgment.acknowledge();
        }
    }

    private AuditEvent mapToAuditEvent(Map<String, Object> payload) {
        String eventId = getStringValue(payload, "eventId");
        String eventTypeStr = getStringValue(payload, "eventType");
        String entityTypeStr = getStringValue(payload, "entityType");
        String entityId = getStringValue(payload, "entityId");
        String actorUserIdStr = getStringValue(payload, "actorUserId");
        String actorUsername = getStringValue(payload, "actorUsername");
        String actorIp = getStringValue(payload, "actorIp");
        String actorDeviceId = getStringValue(payload, "actorDeviceId");
        String actorUserAgent = getStringValue(payload, "actorUserAgent");
        String occurredAtStr = getStringValue(payload, "occurredAt");
        String traceId = getStringValue(payload, "traceId");
        String spanId = getStringValue(payload, "spanId");
        String correlationId = getStringValue(payload, "correlationId");
        String sessionId = getStringValue(payload, "sessionId");
        String serviceName = getStringValue(payload, "serviceName");
        String action = getStringValue(payload, "action");
        String status = getStringValue(payload, "status");
        String errorMessage = getStringValue(payload, "errorMessage");

        @SuppressWarnings("unchecked")
        Map<String, Object> beforeState = (Map<String, Object>) payload.get("beforeState");
        @SuppressWarnings("unchecked")
        Map<String, Object> afterState = (Map<String, Object>) payload.get("afterState");
        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = (Map<String, Object>) payload.get("metadata");

        Map<String, Object> changes = jsonDiffCalculator.calculateDiff(beforeState, afterState);

        EventType eventType = parseEventType(eventTypeStr);
        EntityType entityType = parseEntityType(entityTypeStr);
        UUID actorUserId = actorUserIdStr != null ? UUID.fromString(actorUserIdStr) : null;
        Instant occurredAt = occurredAtStr != null ? Instant.parse(occurredAtStr) : Instant.now();

        return AuditEvent.builder()
                .eventId(eventId != null ? eventId : UUID.randomUUID().toString())
                .eventType(eventType)
                .entityType(entityType)
                .entityId(entityId)
                .actorUserId(actorUserId)
                .actorUsername(actorUsername)
                .actorIp(actorIp)
                .actorDeviceId(actorDeviceId)
                .actorUserAgent(actorUserAgent)
                .beforeState(beforeState)
                .afterState(afterState)
                .changes(changes)
                .occurredAt(occurredAt)
                .traceId(traceId)
                .spanId(spanId)
                .correlationId(correlationId)
                .sessionId(sessionId)
                .serviceName(serviceName)
                .action(action)
                .status(status)
                .errorMessage(errorMessage)
                .metadata(metadata)
                .build();
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private EventType parseEventType(String eventTypeStr) {
        if (eventTypeStr == null) {
            return EventType.ENTITY_ACCESSED;
        }
        try {
            return EventType.valueOf(eventTypeStr);
        } catch (IllegalArgumentException e) {
            logger.warn("Unknown event type: {}, defaulting to ENTITY_ACCESSED", eventTypeStr);
            return EventType.ENTITY_ACCESSED;
        }
    }

    private EntityType parseEntityType(String entityTypeStr) {
        if (entityTypeStr == null) {
            return EntityType.SYSTEM;
        }
        try {
            return EntityType.valueOf(entityTypeStr);
        } catch (IllegalArgumentException e) {
            logger.warn("Unknown entity type: {}, defaulting to SYSTEM", entityTypeStr);
            return EntityType.SYSTEM;
        }
    }
}
