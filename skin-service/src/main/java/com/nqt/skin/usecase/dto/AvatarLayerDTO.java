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
public class AvatarLayerDTO {
    private int layerOrder;
    private Slot slot;
    private String code;
    private String name;
    private String imageUrl;
}
