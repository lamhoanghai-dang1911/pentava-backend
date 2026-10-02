package com.nqt.shop.usecase.boundary;

import com.nqt.shop.usecase.dto.ShopItemResponseDTO;
import com.nqt.shop.usecase.dto.ShopThumbnailResponseDTO;

import java.util.List;

public interface ShopCatalogInputPort {
    List<ShopItemResponseDTO> getCatalog(Long userId);
    List<ShopThumbnailResponseDTO> getShopThumbnails();
}

