package com.nqt.shop.usecase.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CashflowRecordResponseDTO {
    private Long id;
    private String transactionCode;
    private String type; // TOPUP_RUBY, SUBSCRIPTION_PLAN, SHOP_PURCHASE
    private String direction; // IN, OUT
    private long amountVnd;
    private long rubyAmount;
    private Long userId;
    private String userName;
    private String userEmail;
    private String status; // SUCCESS, PENDING, FAILED
    private String paymentMethod;
    private String bankCode;
    private String accountNumber;
    private String accountName;
    private String sepayReferenceCode;
    private String qrCodeUrl;
    private String description;
    private CashflowItemDetailsDTO itemDetails;
    private String createdAt;
    private String completedAt;
}
