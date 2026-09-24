package com.nqt.social.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Post {
    private Long id;
    private Long userId;
    private Long clipId;
    private Long goalId;
    private Integer weekNumber;
    private String videoUrl;
    private String caption;
    private String tags;
    @Builder.Default
    private PrivacyMode privacyMode = PrivacyMode.PUBLIC;
    @Builder.Default
    private long highFiveCount = 0;
    @Builder.Default
    private long commentCount = 0;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
