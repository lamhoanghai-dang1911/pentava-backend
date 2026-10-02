package com.nqt.shop.usecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopItemResponseDTO {
    private Long id;
    private Long skinItemId;
    private String code;
    private String name;
    private String slot;
    private int priceRuby;
    private String imageUrl;
    private String itemBackgroundUrl;
    private String thumbnailUrl;
    private List<String> thumbnailLayers;
    private String description;
    private boolean isOwned;
}
