package com.nqt.shop.usecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashflowSummaryResponseDTO {
    private long totalRevenueVnd;
    private long todayRevenueVnd;
    private long pendingRevenueVnd;
    private long totalTransactions;
    private long successCount;
    private long pendingCount;
    private long failedCount;
    private long rubyMinted;
    private long rubySpent;
    private long rubyCirculating;
    private double conversionRate;
    private RevenueBySourceDTO revenueBySource;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueBySourceDTO {
        private long rubyTopupVnd;
        private long subscriptionVnd;
    }
}
