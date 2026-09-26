package com.nqt.shop.usecase.boundary;

import com.nqt.shop.usecase.dto.*;

import java.util.List;

public interface ShopWalletInputPort {
    WalletResponseDTO getWallet(Long userId);
    TopupInitResponseDTO initTopup(Long userId, Long amountVnd);
    void handleSepayWebhook(SepayWebhookPayloadDTO payload, String authHeader);
    void mockConfirmTopup(String transactionCode);
    List<TopupHistoryResponseDTO> getTopupHistory(Long userId);
}
