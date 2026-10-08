package com.nqt.shop.usecase.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
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

        @JsonProperty("isEnabled")
        @JsonAlias({"enabled", "is_enabled"})
        private boolean isEnabled;

        private String paramValue;

        private String displayLabel;

        public boolean isEnabled() {
            return isEnabled;
        }

        public void setEnabled(boolean enabled) {
            this.isEnabled = enabled;
        }

        public void setIsEnabled(boolean isEnabled) {
            this.isEnabled = isEnabled;
        }
    }
}
