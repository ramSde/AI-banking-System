package com.banking.chat.service.impl;

import com.banking.chat.service.KafkaProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerServiceImpl.class);

    private static final String CHAT_SESSION_CREATED_TOPIC = "banking.chat.session-created";
    private static final String CHAT_MESSAGE_SENT_TOPIC = "banking.chat.message-sent";
    private static final String MESSAGE_FEEDBACK_SUBMITTED_TOPIC = "banking.chat.feedback-submitted";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaProducerServiceImpl(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishChatSessionCreatedEvent(Object event) {
        publishEvent(CHAT_SESSION_CREATED_TOPIC, event);
    }

    @Override
    public void publishChatMessageSentEvent(Object event) {
        publishEvent(CHAT_MESSAGE_SENT_TOPIC, event);
    }

    @Override
    public void publishMessageFeedbackSubmittedEvent(Object event) {
        publishEvent(MESSAGE_FEEDBACK_SUBMITTED_TOPIC, event);
    }

    private void publishEvent(String topic, Object event) {
        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Published event to topic: {} with offset: {}",
                            topic, result.getRecordMetadata().offset());
                } else {
                    logger.error("Failed to publish event to topic: {}", topic, ex);
                }
            });
        } catch (Exception e) {
            logger.error("Error publishing event to topic: {}", topic, e);
        }
    }
}
