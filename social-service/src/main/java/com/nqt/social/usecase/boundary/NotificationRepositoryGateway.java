package com.nqt.social.usecase.boundary;

import com.nqt.social.domain.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationRepositoryGateway {
    Notification save(Notification notification);
    Optional<Notification> findById(Long id);
    List<Notification> findByUserId(Long userId, int page, int size);
    long countUnreadByUserId(Long userId);
    void markAsRead(Long id, Long userId);
    void markAllAsRead(Long userId);
}
