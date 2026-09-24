package com.nqt.social.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nqt.social.domain.SocialEvent;
import com.nqt.social.infrastructure.configuration.KafkaTopicConfig;
import com.nqt.social.usecase.boundary.SocialEventPublisherGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class SocialKafkaProducer implements SocialEventPublisherGateway {

    private static final Logger log = LoggerFactory.getLogger(SocialKafkaProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public SocialKafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    public void publish(SocialEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            String key = event.getTargetUserId() != null ? String.valueOf(event.getTargetUserId()) : "global";

            kafkaTemplate.send(KafkaTopicConfig.SOCIAL_EVENTS_TOPIC, key, json);
            log.info("Bắn sự kiện Kafka thành công: topic={}, type={}, targetUserId={}",
                    KafkaTopicConfig.SOCIAL_EVENTS_TOPIC, event.getEventType(), event.getTargetUserId());

        } catch (Exception e) {
            log.error("Không thể serialize hoặc gửi sự kiện Kafka: {}", e.getMessage(), e);
        }
    }
}
