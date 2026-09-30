package com.nqt.manage.usecase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformMetricRequestDTO {
    @NotBlank(message = "Mã chỉ số không được để trống")
    private String metricKey;

    @NotBlank(message = "Nhãn hiển thị không được để trống")
    private String label;

    @NotNull(message = "Giá trị số liệu không được để trống")
    private Long counterValue;

    private String suffix;
    private String icon;
    private Boolean isAutoCalculated;
    private Integer displayOrder;
    private Boolean isActive;
}
