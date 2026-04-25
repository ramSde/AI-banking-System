package com.banking.notification.event;

import com.banking.notification.dto.NotificationSendRequest;
import com.banking.notification.service.NotificationService;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class NotificationEventConsumer {

    private final NotificationService notificationService;
    private final Tracer tracer;

    public NotificationEventConsumer(NotificationService notificationService, Tracer tracer) {
        this.notificationService = notificationService;
        this.tracer = tracer;
    }

    @KafkaListener(
            topicPattern = "banking\\..*\\.notification-requested",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeNotificationEvent(
            @Payload NotificationRequestedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment
    ) {
        UUID traceId = event.getCorrelationId() != null ? event.getCorrelationId() : UUID.randomUUID();

        log.info("Received notification event from topic: {}, eventId: {}, correlationId: {}",
                topic, event.getEventId(), traceId);

        try {
            NotificationRequestedEvent.NotificationPayload payload = event.getPayload();

            NotificationSendRequest request = new NotificationSendRequest(
                    payload.getUserId(),
                    payload.getTemplateCode(),
                    payload.getChannel(),
                    payload.getRecipient(),
                    payload.getVariables(),
                    event.getCorrelationId()
            );

            UUID idempotencyKey = event.getEventId() != null ? event.getEventId() : UUID.randomUUID();

            notificationService.sendNotification(request, idempotencyKey, traceId);

            acknowledgment.acknowledge();
            log.info("Successfully processed notification event: {}", event.getEventId());

        } catch (Exception e) {
            log.error("Failed to process notification event: {} - {}", event.getEventId(), e.getMessage(), e);
            acknowledgment.acknowledge();
        }
    }
}
