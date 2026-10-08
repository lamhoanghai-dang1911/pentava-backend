package com.nqt.shop.usecase.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePlanFeatureConfigRequestDTO {

    private List<FeatureConfigItem> featureConfigs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeatureConfigItem {
        @NotBlank(message = "Mã tính năng (featureCode) không được để trống")
        private String featureCode;

        private boolean isEnabled;

        private String paramValue;

        private String displayLabel;
    }
}
