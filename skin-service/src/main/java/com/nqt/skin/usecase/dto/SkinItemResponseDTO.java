package com.nqt.skin.usecase.dto;

import com.nqt.skin.domain.model.Slot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkinItemResponseDTO {
    private Long id;
    private String code;
    private String name;
    private Slot slot;
    private int layerOrder;
    private String imageUrl;
    private int priceRuby;
    private String description;
    private Boolean isEquipped;
}
