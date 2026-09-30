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
public class SiteConfigRequestDTO {
    @NotBlank(message = "Khóa cấu hình không được để trống")
    private String configKey;
    @NotBlank(message = "Giá trị cấu hình không được để trống")
    private String configValue;
    private String groupName;
    private String description;
    private Boolean isPublic;
}
