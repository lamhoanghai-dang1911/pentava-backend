package com.nqt.shop.usecase.dto;

import com.nqt.shop.domain.model.TopupStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopupInitResponseDTO {
    private String transactionCode;
    private long amountVnd;
    private long rubyAmount;
    private String bankCode;
    private String accountNumber;
    private String accountName;
    private String transferContent;
    private String qrCodeUrl;
    private TopupStatus status;
}
