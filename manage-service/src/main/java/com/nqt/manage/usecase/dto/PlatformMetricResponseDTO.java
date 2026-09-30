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
public class PlatformMetricResponseDTO {
    private Long id;
    private String metricKey;
    private String label;
    private Long counterValue;
    private String suffix;
    private String icon;
    private Boolean isAutoCalculated;
    private Integer displayOrder;
    private Boolean isActive;
    private LocalDateTime updatedAt;
}
