package com.nqt.social.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class HighFive {
    private Long id;
    private Long postId;
    private Long userId;
    private LocalDateTime createdAt;
}
