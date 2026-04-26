package com.banking.chat.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic chatSessionCreatedTopic() {
        return TopicBuilder.name("banking.chat.session-created")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic chatMessageSentTopic() {
        return TopicBuilder.name("banking.chat.message-sent")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic messageFeedbackSubmittedTopic() {
        return TopicBuilder.name("banking.chat.feedback-submitted")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic chatSessionCreatedRetryTopic() {
        return TopicBuilder.name("banking.chat.session-created.retry")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic chatSessionCreatedDlqTopic() {
        return TopicBuilder.name("banking.chat.session-created.dlq")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic chatMessageSentRetryTopic() {
        return TopicBuilder.name("banking.chat.message-sent.retry")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic chatMessageSentDlqTopic() {
        return TopicBuilder.name("banking.chat.message-sent.dlq")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
