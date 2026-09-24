package com.nqt.social.infrastructure.persistence.adapter;

import com.nqt.social.domain.Notification;
import com.nqt.social.infrastructure.persistence.entity.NotificationEntity;
import com.nqt.social.infrastructure.persistence.repository.NotificationJpaRepository;
import com.nqt.social.usecase.boundary.NotificationRepositoryGateway;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class NotificationRepositoryJpaAdapter implements NotificationRepositoryGateway {

    private final NotificationJpaRepository jpaRepository;

    public NotificationRepositoryJpaAdapter(NotificationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Notification save(Notification notification) {
        NotificationEntity entity = toEntity(notification);
        NotificationEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Notification> findByUserId(Long userId, int page, int size) {
        int pageIndex = Math.max(0, page);
        int pageSize = (size > 0 && size <= 100) ? size : 20;
        Pageable pageable = PageRequest.of(pageIndex, pageSize);

        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countUnreadByUserId(Long userId) {
        return jpaRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public void markAsRead(Long id, Long userId) {
        jpaRepository.markAsRead(id, userId);
    }

    @Override
    public void markAllAsRead(Long userId) {
        jpaRepository.markAllAsRead(userId);
    }

    private Notification toDomain(NotificationEntity e) {
        return Notification.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .actorId(e.getActorId())
                .actorName(e.getActorName())
                .actorAvatar(e.getActorAvatar())
                .eventType(e.getEventType())
                .postId(e.getPostId())
                .commentId(e.getCommentId())
                .message(e.getMessage())
                .isRead(e.isRead())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private NotificationEntity toEntity(Notification d) {
        return NotificationEntity.builder()
                .id(d.getId())
                .userId(d.getUserId())
                .actorId(d.getActorId())
                .actorName(d.getActorName())
                .actorAvatar(d.getActorAvatar())
                .eventType(d.getEventType())
                .postId(d.getPostId())
                .commentId(d.getCommentId())
                .message(d.getMessage())
                .isRead(d.isRead())
                .createdAt(d.getCreatedAt())
                .build();
    }
}
