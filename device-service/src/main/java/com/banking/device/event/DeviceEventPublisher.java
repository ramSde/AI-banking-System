package com.banking.device.event;

import com.banking.device.domain.Device;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Publisher for device-related Kafka events.
 * Publishes events to Kafka topics for async processing.
 */
@Slf4j
@Component
public class DeviceEventPublisher {

    private static final String TOPIC_DEVICE_REGISTERED = "banking.device.device-registered";
    private static final String TOPIC_TRUST_CHANGED = "banking.device.trust-changed";
    private static final String TOPIC_ANOMALY_DETECTED = "banking.device.anomaly-detected";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public DeviceEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Publish device registered event
     */
    public void publishDeviceRegistered(Device device) {
        try {
            Map<String, Object> event = createBaseEvent("DeviceRegistered");
            Map<String, Object> payload = new HashMap<>();
            payload.put("deviceId", device.getId().toString());
            payload.put("userId", device.getUserId().toString());
            payload.put("deviceType", device.getDeviceType().toString());
            payload.put("trustScore", device.getTrustScore());
            payload.put("ipAddress", device.getIpAddress());
            event.put("payload", payload);

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC_DEVICE_REGISTERED, device.getId().toString(), eventJson);
            log.info("Published device registered event for device: {}", device.getId());
        } catch (Exception e) {
            log.error("Failed to publish device registered event", e);
        }
    }

    /**
     * Publish trust changed event
     */
    public void publishTrustChanged(Device device, Integer oldScore, Integer newScore) {
        try {
            Map<String, Object> event = createBaseEvent("TrustChanged");
            Map<String, Object> payload = new HashMap<>();
            payload.put("deviceId", device.getId().toString());
            payload.put("userId", device.getUserId().toString());
            payload.put("oldTrustScore", oldScore);
            payload.put("newTrustScore", newScore);
            payload.put("change", newScore - oldScore);
            event.put("payload", payload);

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC_TRUST_CHANGED, device.getId().toString(), eventJson);
            log.info("Published trust changed event for device: {}", device.getId());
        } catch (Exception e) {
            log.error("Failed to publish trust changed event", e);
        }
    }

    /**
     * Publish anomaly detected event
     */
    public void publishAnomalyDetected(UUID deviceId, UUID userId, String anomalyType, String severity) {
        try {
            Map<String, Object> event = createBaseEvent("AnomalyDetected");
            Map<String, Object> payload = new HashMap<>();
            payload.put("deviceId", deviceId.toString());
            payload.put("userId", userId.toString());
            payload.put("anomalyType", anomalyType);
            payload.put("severity", severity);
            event.put("payload", payload);

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC_ANOMALY_DETECTED, deviceId.toString(), eventJson);
            log.info("Published anomaly detected event for device: {}", deviceId);
        } catch (Exception e) {
            log.error("Failed to publish anomaly detected event", e);
        }
    }

    /**
     * Create base event structure
     */
    private Map<String, Object> createBaseEvent(String eventType) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", eventType);
        event.put("version", "1.0");
        event.put("occurredAt", Instant.now().toString());
        event.put("correlationId", UUID.randomUUID().toString());
        return event;
    }
}