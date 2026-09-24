package com.nqt.social.usecase.dto;

import com.nqt.social.domain.FriendshipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendshipDTO {
    private Long id;
    private Long requesterId;
    private Long addresseeId;
    private Long friendUserId;
    private String friendName;
    private String friendEmail;
    private String friendAvatar;
    private FriendshipStatus status;
    private LocalDateTime createdAt;
}
