package com.nqt.skin.usecase.dto;

import com.nqt.skin.domain.model.Slot;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnequipSkinRequestDTO {
    @NotNull(message = "slot không được để trống")
    private Slot slot;
}
