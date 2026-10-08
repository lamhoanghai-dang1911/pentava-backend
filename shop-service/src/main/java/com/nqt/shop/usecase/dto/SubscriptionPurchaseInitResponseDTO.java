package com.nqt.shop.usecase.dto;

import com.nqt.shop.domain.model.TopupStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPurchaseInitResponseDTO {

    private String transactionCode;

    private Long amountVnd;

    private String planCode;

    private String planName;

    private int durationDays;

    private String description;

    private String bankCode;

    private String accountNumber;

    private String accountName;

    private String transferContent;

    private String qrCodeUrl;

    private TopupStatus status;

    private LocalDateTime createdAt;
}
