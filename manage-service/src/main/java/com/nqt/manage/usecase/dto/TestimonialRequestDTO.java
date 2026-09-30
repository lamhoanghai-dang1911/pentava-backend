package com.nqt.manage.usecase.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestimonialRequestDTO {
    @NotBlank(message = "Tên người đánh giá không được để trống")
    private String authorName;
    private String authorTitle;
    private String avatarUrl;
    @NotBlank(message = "Nội dung đánh giá không được để trống")
    private String content;
    @Min(value = 1, message = "Số sao tối thiểu là 1")
    @Max(value = 5, message = "Số sao tối đa là 5")
    private Integer rating;
    private Long userId;
    private Boolean isFeatured;
    private Integer displayOrder;
    private Boolean isActive;
}
