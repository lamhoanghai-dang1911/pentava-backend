package com.nqt.manage.usecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestimonialResponseDTO {
    private Long id;
    private String authorName;
    private String authorTitle;
    private String avatarUrl;
    private String content;
    private Integer rating;
    private Long userId;
    private Boolean isFeatured;
    private Integer displayOrder;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
