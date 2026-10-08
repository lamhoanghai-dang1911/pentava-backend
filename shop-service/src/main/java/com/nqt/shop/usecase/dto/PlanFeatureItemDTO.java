package com.nqt.shop.usecase.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("isEnabled")
    private boolean isEnabled;

    private String paramValue;
    private String displayLabel;
    private String unit;

    @JsonProperty("enabled")
    public boolean getEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }
}
