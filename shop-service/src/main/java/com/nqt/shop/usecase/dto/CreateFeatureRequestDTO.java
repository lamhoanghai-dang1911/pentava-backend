package com.nqt.shop.usecase.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFeatureRequestDTO {

    @NotBlank(message = "Mã tính năng (featureCode) không được để trống")
    private String featureCode;

    @NotBlank(message = "Tên tính năng không được để trống")
    private String name;

    private String description;

    private String defaultValue;

    private String unit;

    private Integer displayOrder;
}
