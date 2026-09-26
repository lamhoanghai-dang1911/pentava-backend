package com.nqt.skin.usecase.interactor;

import com.nqt.skin.domain.exception.SkinNotFoundException;
import com.nqt.skin.domain.exception.SkinNotOwnedException;
import com.nqt.skin.domain.model.Slot;
import com.nqt.skin.infrastructure.persistence.entity.SkinItemEntity;
import com.nqt.skin.infrastructure.persistence.entity.UserEquippedSkinEntity;
import com.nqt.skin.infrastructure.persistence.entity.UserInventoryEntity;
import com.nqt.skin.infrastructure.persistence.repository.SkinItemRepository;
import com.nqt.skin.infrastructure.persistence.repository.UserEquippedSkinRepository;
import com.nqt.skin.infrastructure.persistence.repository.UserInventoryRepository;
import com.nqt.skin.usecase.boundary.SkinInputPort;
import com.nqt.skin.usecase.dto.AvatarLayerDTO;
import com.nqt.skin.usecase.dto.AvatarRenderResponseDTO;
import com.nqt.skin.usecase.dto.SkinItemResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkinInteractor implements SkinInputPort {

    private final SkinItemRepository skinItemRepository;
    private final UserInventoryRepository userInventoryRepository;
    private final UserEquippedSkinRepository userEquippedSkinRepository;

    @Override
    @Transactional(readOnly = true)
    public AvatarRenderResponseDTO getAvatar(Long userId) {
        List<AvatarLayerDTO> layers = new ArrayList<>();

        // 1. Luôn thêm layer 0: Mèo cơ bản (cat_base)
        skinItemRepository.findByCode("cat_base").ifPresent(base -> layers.add(
                AvatarLayerDTO.builder()
                        .layerOrder(base.getLayerOrder())
                        .slot(base.getSlot())
                        .code(base.getCode())
                        .name(base.getName())
                        .imageUrl(base.getImageUrl())
                        .build()
        ));

        // 2. Thêm các món user đã trang bị (sắp xếp theo layer_order ASC)
        if (userId != null) {
            List<UserEquippedSkinEntity> equipped = userEquippedSkinRepository.findByUserIdOrderBySkinItemLayerOrderAsc(userId);
            for (UserEquippedSkinEntity eq : equipped) {
                SkinItemEntity item = eq.getSkinItem();
                // Bỏ qua base nếu lỡ lưu để tránh trùng
                if (item.getSlot() == Slot.BASE) {
                    continue;
                }
                layers.add(
                        AvatarLayerDTO.builder()
                                .layerOrder(item.getLayerOrder())
                                .slot(item.getSlot())
                                .code(item.getCode())
                                .name(item.getName())
                                .imageUrl(item.getImageUrl())
                                .build()
                );
            }
        }

        // Đảm bảo sắp xếp nghiêm ngặt theo layerOrder tăng dần (0: cat_base -> 1: neck -> 2: eyes -> 3: head -> 4: accessory)
        layers.sort(Comparator.comparingInt(AvatarLayerDTO::getLayerOrder));

        return AvatarRenderResponseDTO.builder()
                .userId(userId)
                .layers(layers)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkinItemResponseDTO> getAllSkins() {
        return skinItemRepository.findByIsActiveTrueOrderByLayerOrderAsc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkinItemResponseDTO> getUserInventory(Long userId) {
        List<UserInventoryEntity> inventory = userInventoryRepository.findByUserId(userId);
        Set<Long> equippedSkinIds = userEquippedSkinRepository.findByUserIdOrderBySkinItemLayerOrderAsc(userId).stream()
                .map(eq -> eq.getSkinItem().getId())
                .collect(Collectors.toSet());

        return inventory.stream()
                .map(ui -> {
                    SkinItemEntity item = ui.getSkinItem();
                    SkinItemResponseDTO dto = toDto(item);
                    dto.setIsEquipped(equippedSkinIds.contains(item.getId()));
                    return dto;
                })
                .sorted(Comparator.comparingInt(SkinItemResponseDTO::getLayerOrder))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AvatarRenderResponseDTO equipSkin(Long userId, Long skinId) {
        SkinItemEntity skin = skinItemRepository.findById(skinId)
                .orElseThrow(() -> new SkinNotFoundException("Không tìm thấy skin với ID: " + skinId));

        if (skin.getSlot() == Slot.BASE) {
            throw new IllegalArgumentException("Không thể trang bị nhân vật cơ bản như một trang phục");
        }

        // Kiểm tra user có sở hữu skin này không
        boolean owned = userInventoryRepository.existsByUserIdAndSkinItemId(userId, skinId);
        if (!owned) {
            throw new SkinNotOwnedException("Bạn chưa sở hữu skin này: " + skin.getName());
        }

        // Trang bị vào slot (thay thế nếu đã có)
        Optional<UserEquippedSkinEntity> existing = userEquippedSkinRepository.findByUserIdAndSlot(userId, skin.getSlot());
        if (existing.isPresent()) {
            UserEquippedSkinEntity eq = existing.get();
            eq.setSkinItem(skin);
            eq.setEquippedAt(LocalDateTime.now());
            userEquippedSkinRepository.save(eq);
        } else {
            UserEquippedSkinEntity newEq = UserEquippedSkinEntity.builder()
                    .userId(userId)
                    .slot(skin.getSlot())
                    .skinItem(skin)
                    .equippedAt(LocalDateTime.now())
                    .build();
            userEquippedSkinRepository.save(newEq);
        }

        log.info("User {} equipped skin {} ({}) to slot {}", userId, skin.getId(), skin.getName(), skin.getSlot());
        return getAvatar(userId);
    }

    @Override
    @Transactional
    public AvatarRenderResponseDTO unequipSkin(Long userId, Slot slot) {
        if (slot == Slot.BASE) {
            throw new IllegalArgumentException("Không thể tháo nhân vật gốc (BASE)");
        }
        userEquippedSkinRepository.deleteByUserIdAndSlot(userId, slot);
        log.info("User {} unequipped skin from slot {}", userId, slot);
        return getAvatar(userId);
    }

    @Override
    @Transactional
    public void grantSkin(Long userId, Long skinItemId) {
        SkinItemEntity skin = skinItemRepository.findById(skinItemId)
                .orElseThrow(() -> new SkinNotFoundException("Không tìm thấy skin với ID: " + skinItemId));

        if (userInventoryRepository.existsByUserIdAndSkinItemId(userId, skinItemId)) {
            log.info("User {} already owns skin {}", userId, skinItemId);
            return;
        }

        UserInventoryEntity entity = UserInventoryEntity.builder()
                .userId(userId)
                .skinItem(skin)
                .acquiredAt(LocalDateTime.now())
                .build();
        userInventoryRepository.save(entity);
        log.info("Granted skin {} ({}) to user {}", skinItemId, skin.getName(), userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSkinOwned(Long userId, Long skinItemId) {
        return userInventoryRepository.existsByUserIdAndSkinItemId(userId, skinItemId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getOwnedSkinIds(Long userId) {
        return userInventoryRepository.findSkinItemIdsByUserId(userId);
    }

    private SkinItemResponseDTO toDto(SkinItemEntity entity) {
        return SkinItemResponseDTO.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .slot(entity.getSlot())
                .layerOrder(entity.getLayerOrder())
                .imageUrl(entity.getImageUrl())
                .priceRuby(entity.getPriceRuby())
                .description(entity.getDescription())
                .build();
    }
}
