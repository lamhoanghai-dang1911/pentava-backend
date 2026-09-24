package com.nqt.social.usecase.interactor;

import com.nqt.social.domain.Notification;
import com.nqt.social.domain.SocialEvent;
import com.nqt.social.usecase.boundary.NotificationInputPort;
import com.nqt.social.usecase.boundary.NotificationRepositoryGateway;
import com.nqt.social.usecase.dto.NotificationDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationInteractor implements NotificationInputPort {

    private final NotificationRepositoryGateway notificationRepositoryGateway;

    public NotificationInteractor(NotificationRepositoryGateway notificationRepositoryGateway) {
        this.notificationRepositoryGateway = notificationRepositoryGateway;
    }

    @Override
    @Transactional
    public NotificationDTO saveNotification(SocialEvent event) {
        Notification notification = Notification.builder()
                .userId(event.getTargetUserId())
                .actorId(event.getActorId())
                .actorName(event.getActorName())
                .actorAvatar(event.getActorAvatar())
                .eventType(event.getEventType())
                .postId(event.getPostId())
                .commentId(event.getCommentId())
                .message(event.getMessage())
                .isRead(false)
                .createdAt(event.getTimestamp() != null ? event.getTimestamp() : LocalDateTime.now())
                .build();

        Notification saved = notificationRepositoryGateway.save(notification);
        return toDto(saved);
    }

    @Override
    public List<NotificationDTO> getNotifications(Long userId, int page, int size) {
        return notificationRepositoryGateway.findByUserId(userId, page, size).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepositoryGateway.countUnreadByUserId(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        notificationRepositoryGateway.markAsRead(notificationId, userId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepositoryGateway.markAllAsRead(userId);
    }

    private NotificationDTO toDto(Notification n) {
        return NotificationDTO.builder()
                .id(n.getId())
                .userId(n.getUserId())
                .actorId(n.getActorId())
                .actorName(n.getActorName())
                .actorAvatar(n.getActorAvatar())
                .eventType(n.getEventType())
                .postId(n.getPostId())
                .commentId(n.getCommentId())
                .message(n.getMessage())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
