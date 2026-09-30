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
public class FeatureRequestDTO {
    @NotBlank(message = "Tiêu đề tính năng không được để trống")
    private String title;
    @NotBlank(message = "Mô tả tính năng không được để trống")
    private String description;
    private String icon;
    private String tag;
    private String previewImageUrl;
    private Integer displayOrder;
    private Boolean isActive;
}
