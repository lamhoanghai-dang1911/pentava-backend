package com.nqt.social.usecase.dto;

import com.nqt.social.domain.SocialEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private Long userId;
    private Long actorId;
    private String actorName;
    private String actorAvatar;
    private SocialEvent.EventType eventType;
    private Long postId;
    private Long commentId;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
}
