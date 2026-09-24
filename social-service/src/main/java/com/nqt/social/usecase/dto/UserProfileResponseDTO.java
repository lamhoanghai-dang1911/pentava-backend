package com.nqt.social.usecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponseDTO {
    private Long userId;
    private String name;
    private String email;
    private String avatarUrl;
    private String bio;
    private long totalPosts;
    private long totalHighFivesReceived;
    private long totalFriends;
    private String friendshipStatusWithMe; // NONE, PENDING_SENT, PENDING_RECEIVED, FRIENDS
    private boolean isMe;
}
