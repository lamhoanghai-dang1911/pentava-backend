package com.nqt.social.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialEvent {
    public enum EventType {
        HIGH_FIVE,
        COMMENT,
        FRIEND_REQUEST,
        FRIEND_ACCEPTED
    }

    private EventType eventType;
    private Long actorId;
    private String actorName;
    private String actorAvatar;
    private Long targetUserId;
    private Long postId;
    private Long commentId;
    private String message;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
