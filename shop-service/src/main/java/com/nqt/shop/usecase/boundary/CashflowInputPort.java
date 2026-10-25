package com.nqt.shop.usecase.boundary;

import com.nqt.shop.infrastructure.persistence.entity.ShopItemEntity;
import com.nqt.shop.infrastructure.persistence.entity.SubscriptionOrderEntity;
import com.nqt.shop.infrastructure.persistence.entity.TopupTransactionEntity;
import com.nqt.shop.usecase.dto.CashflowRecordResponseDTO;
import com.nqt.shop.usecase.dto.CashflowSummaryResponseDTO;

import java.util.List;

public interface CashflowInputPort {
    CashflowRecordResponseDTO recordTopupPending(TopupTransactionEntity tx);
    CashflowRecordResponseDTO recordTopupSuccess(TopupTransactionEntity tx);
    CashflowRecordResponseDTO recordSubscriptionPending(SubscriptionOrderEntity order);
    CashflowRecordResponseDTO recordSubscriptionSuccess(SubscriptionOrderEntity order);
    CashflowRecordResponseDTO recordShopPurchase(Long userId, ShopItemEntity item);

    List<CashflowRecordResponseDTO> getTransactions(String direction, String type, String status, String searchTerm, String dateRange);
    CashflowSummaryResponseDTO getCashflowSummary();
    CashflowRecordResponseDTO approveTransaction(String transactionCode);
}
