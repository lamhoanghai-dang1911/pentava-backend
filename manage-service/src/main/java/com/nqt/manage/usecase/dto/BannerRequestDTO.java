package com.nqt.manage.usecase.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerRequestDTO {
    @NotBlank(message = "Tiêu đề banner không được để trống")
    private String title;
    private String subtitle;
    @NotBlank(message = "Đường dẫn hình ảnh không được để trống")
    private String imageUrl;
    private String ctaText;
    private String ctaLink;
    private Integer displayOrder;
    private Boolean isActive;
}
