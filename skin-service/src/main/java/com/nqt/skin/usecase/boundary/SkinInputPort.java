package com.nqt.skin.usecase.boundary;

import com.nqt.skin.domain.model.Slot;
import com.nqt.skin.usecase.dto.AvatarRenderResponseDTO;
import com.nqt.skin.usecase.dto.SkinItemResponseDTO;

import java.util.List;

public interface SkinInputPort {
    AvatarRenderResponseDTO getAvatar(Long userId);
    List<SkinItemResponseDTO> getAllSkins();
    List<SkinItemResponseDTO> getUserInventory(Long userId);
    AvatarRenderResponseDTO equipSkin(Long userId, Long skinId);
    AvatarRenderResponseDTO unequipSkin(Long userId, Slot slot);
    void grantSkin(Long userId, Long skinItemId);
    boolean isSkinOwned(Long userId, Long skinItemId);
    List<Long> getOwnedSkinIds(Long userId);
}
