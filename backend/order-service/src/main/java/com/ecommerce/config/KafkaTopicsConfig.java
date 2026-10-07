package com.ecommerce.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Creates the order topics on startup if they don't exist yet (broker defaults for partitions/replicas).
 */
@Configuration
public class KafkaTopicsConfig {

    @Bean
    public NewTopic orderPlacedTopic(@Value("${app.kafka.topics.order-placed}") String name) {
        return TopicBuilder.name(name).build();
    }

    @Bean
    public NewTopic orderCancelledTopic(@Value("${app.kafka.topics.order-cancelled}") String name) {
        return TopicBuilder.name(name).build();
    }
}
