package com.banking.rag.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String RAG_QUERY_TOPIC = "banking.rag.query.created";
    public static final String RAG_CONTEXT_TOPIC = "banking.rag.context.assembled";
    public static final String RAG_CACHE_TOPIC = "banking.rag.cache.hit";

    @Bean
    public NewTopic ragQueryTopic() {
        return TopicBuilder.name(RAG_QUERY_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic ragContextTopic() {
        return TopicBuilder.name(RAG_CONTEXT_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic ragCacheTopic() {
        return TopicBuilder.name(RAG_CACHE_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
