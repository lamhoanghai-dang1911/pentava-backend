package com.nqt.manage.usecase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingPackageRequestDTO {
    @NotBlank(message = "Tên gói không được để trống")
    private String name;
    @NotNull(message = "Số lượng Ruby không được để trống")
    private Integer rubyAmount;
    private Integer bonusRuby;
    @NotNull(message = "Giá tiền không được để trống")
    private BigDecimal priceVnd;
    private String tag;
    private String features;
    private String ctaText;
    private String ctaLink;
    private Integer displayOrder;
    private Boolean isActive;
}
