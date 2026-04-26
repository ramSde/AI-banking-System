package com.banking.chat.service;

public interface KafkaProducerService {

    void publishChatSessionCreatedEvent(Object event);

    void publishChatMessageSentEvent(Object event);

    void publishMessageFeedbackSubmittedEvent(Object event);
}
