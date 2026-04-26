package com.banking.insight.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topics.insight-generated}")
    private String insightGeneratedTopic;

    @Value("${kafka.topics.anomaly-detected}")
    private String anomalyDetectedTopic;

    @Value("${kafka.topics.recommendation-created}")
    private String recommendationCreatedTopic;

    @Value("${kafka.topics.pattern-identified}")
    private String patternIdentifiedTopic;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        final Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public NewTopic insightGeneratedTopic() {
        return TopicBuilder.name(insightGeneratedTopic)
            .partitions(3)
            .replicas(3)
            .build();
    }

    @Bean
    public NewTopic anomalyDetectedTopic() {
        return TopicBuilder.name(anomalyDetectedTopic)
            .partitions(3)
            .replicas(3)
            .build();
    }

    @Bean
    public NewTopic recommendationCreatedTopic() {
        return TopicBuilder.name(recommendationCreatedTopic)
            .partitions(3)
            .replicas(3)
            .build();
    }

    @Bean
    public NewTopic patternIdentifiedTopic() {
        return TopicBuilder.name(patternIdentifiedTopic)
            .partitions(3)
            .replicas(3)
            .build();
    }
}
