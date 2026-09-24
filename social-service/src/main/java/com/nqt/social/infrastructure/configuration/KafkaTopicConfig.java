package com.nqt.social.infrastructure.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String SOCIAL_EVENTS_TOPIC = "social.events";

    @Bean
    public NewTopic socialEventsTopic() {
        return TopicBuilder.name(SOCIAL_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
