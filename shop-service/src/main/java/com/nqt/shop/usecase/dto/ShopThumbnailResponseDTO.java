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
public class ShopThumbnailResponseDTO {
    private Long shopItemId;
    private Long skinItemId;
    private String code;
    private String name;
    private String slot;
    private String itemBackgroundUrl;
    private String thumbnailUrl;
    private List<String> thumbnailLayers;
}
