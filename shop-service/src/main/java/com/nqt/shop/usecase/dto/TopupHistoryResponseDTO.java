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
public class TopupHistoryResponseDTO {
    private Long id;
    private String transactionCode;
    private long amountVnd;
    private long rubyAmount;
    private TopupStatus status;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
