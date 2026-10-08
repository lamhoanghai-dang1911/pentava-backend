package com.nqt.manage.usecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanFeatureItemDTO {
    private Long featureId;
    private String featureCode;
    private String name;
    private String description;
    private boolean isEnabled;
    private String paramValue;
    private String displayLabel;
    private String unit;
}
