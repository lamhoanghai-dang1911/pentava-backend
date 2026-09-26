package com.nqt.shop.usecase.boundary;

import com.nqt.shop.usecase.dto.ShopItemResponseDTO;

import java.util.List;

public interface ShopCatalogInputPort {
    List<ShopItemResponseDTO> getCatalog(Long userId);
}
