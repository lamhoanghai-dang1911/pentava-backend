package com.nqt.social.usecase.dto;

import com.nqt.social.domain.PrivacyMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDTO {
    private Long id;
    private Long userId;
    private String authorName;
    private String authorAvatar;
    private Long clipId;
    private Long goalId;
    private Integer weekNumber;
    private String videoUrl;
    private String caption;
    private String tags;
    private PrivacyMode privacyMode;
    private long highFiveCount;
    private long commentCount;
    private boolean isHighFivedByMe;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
