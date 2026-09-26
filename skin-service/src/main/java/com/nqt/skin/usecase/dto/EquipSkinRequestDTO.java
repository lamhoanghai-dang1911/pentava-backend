package com.nqt.skin.usecase.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipSkinRequestDTO {
    @NotNull(message = "skinId không được để trống")
    private Long skinId;
}
