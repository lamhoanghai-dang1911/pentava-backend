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
public class SubscriptionPurchaseInitRequestDTO {

    @NotBlank(message = "Mã gói cước (planCode) không được để trống (ví dụ: BASIC, GOLD, PREMIUM)")
    private String planCode;
}
