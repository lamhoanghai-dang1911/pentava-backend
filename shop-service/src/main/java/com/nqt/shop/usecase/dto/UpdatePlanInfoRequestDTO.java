package com.nqt.shop.usecase.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class UpdatePlanInfoRequestDTO {

    @NotBlank(message = "Tên gói không được để trống")
    private String name;

    @Min(value = 0, message = "Giá gói không được âm")
    private Long priceVnd;

    @Min(value = 1, message = "Thời hạn gói tối thiểu 1 ngày")
    private Integer durationDays;

    private String description;

    private String badge;

    @JsonProperty("isActive")
    @JsonAlias({ "active", "is_active" })
    private Boolean isActive;

    private Integer displayOrder;

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        this.isActive = active;
    }
}
