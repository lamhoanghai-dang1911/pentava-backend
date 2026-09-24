package com.nqt.social.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nqt.social.domain.SocialEvent;
import com.nqt.social.infrastructure.configuration.KafkaTopicConfig;
import com.nqt.social.infrastructure.realtime.SseNotificationService;
import com.nqt.social.usecase.boundary.NotificationInputPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SocialKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(SocialKafkaConsumer.class);

    private final SseNotificationService sseNotificationService;
    private final NotificationInputPort notificationInputPort;
    private final ObjectMapper objectMapper;

    public SocialKafkaConsumer(SseNotificationService sseNotificationService,
                               NotificationInputPort notificationInputPort) {
        this.sseNotificationService = sseNotificationService;
        this.notificationInputPort = notificationInputPort;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @KafkaListener(topics = KafkaTopicConfig.SOCIAL_EVENTS_TOPIC, groupId = "${spring.kafka.consumer.group-id:social-service-group}")
    public void consume(String message) {
        try {
            SocialEvent event = objectMapper.readValue(message, SocialEvent.class);
            log.info("Nhận sự kiện Kafka: type={}, targetUserId={}, actorName={}",
                    event.getEventType(), event.getTargetUserId(), event.getActorName());

            if (event.getTargetUserId() != null) {
                // 1. Lưu thông báo vào CSDL (bảng notifications)
                try {
                    notificationInputPort.saveNotification(event);
                } catch (Exception e) {
                    log.error("Lỗi khi lưu thông báo vào database: {}", e.getMessage(), e);
                }

                // 2. Đẩy thông báo thời gian thực tới client của user nhận thông qua SSE
                sseNotificationService.sendNotification(event.getTargetUserId(), event);
            }

        } catch (Exception e) {
            log.error("Lỗi khi parse hoặc xử lý sự kiện Kafka: {}", e.getMessage(), e);
        }
    }
}
