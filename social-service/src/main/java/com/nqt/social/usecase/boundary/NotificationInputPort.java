package com.nqt.social.usecase.boundary;

import com.nqt.social.domain.SocialEvent;
import com.nqt.social.usecase.dto.NotificationDTO;

import java.util.List;

public interface NotificationInputPort {
    NotificationDTO saveNotification(SocialEvent event);
    List<NotificationDTO> getNotifications(Long userId, int page, int size);
    long getUnreadCount(Long userId);
    void markAsRead(Long notificationId, Long userId);
    void markAllAsRead(Long userId);
}
