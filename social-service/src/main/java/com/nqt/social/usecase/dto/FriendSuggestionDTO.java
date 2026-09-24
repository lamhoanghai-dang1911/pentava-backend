package com.nqt.social.usecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendSuggestionDTO {
    private Long userId;
    private String name;
    private String email;
    private String avatarUrl;
    private String bio;
    private int mutualFriendsCount;
    private List<String> mutualFriendNames;
    private long totalPosts;
    private String reason;
}
