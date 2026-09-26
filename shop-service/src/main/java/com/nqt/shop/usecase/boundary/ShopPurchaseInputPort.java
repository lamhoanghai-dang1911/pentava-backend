package com.nqt.shop.usecase.boundary;

import com.nqt.shop.usecase.dto.BuyItemResponseDTO;

public interface ShopPurchaseInputPort {
    BuyItemResponseDTO buyItem(Long userId, Long skinItemId);
}
