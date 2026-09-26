package com.nqt.shop.usecase.interactor;

import com.nqt.shop.infrastructure.persistence.entity.ShopItemEntity;
import com.nqt.shop.infrastructure.persistence.repository.ShopItemRepository;
import com.nqt.shop.usecase.boundary.ShopCatalogInputPort;
import com.nqt.shop.usecase.boundary.SkinServiceGateway;
import com.nqt.shop.usecase.dto.ShopItemResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopCatalogInteractor implements ShopCatalogInputPort {

    private final ShopItemRepository shopItemRepository;
    private final SkinServiceGateway skinServiceGateway;

    @Override
    @Transactional(readOnly = true)
    public List<ShopItemResponseDTO> getCatalog(Long userId) {
        List<ShopItemEntity> items = shopItemRepository.findByIsForSaleTrue();

        // Lấy danh sách ID skin mà user đã sở hữu từ skin-service qua gRPC
        Set<Long> ownedSkinIds;
        if (userId != null) {
            ownedSkinIds = new HashSet<>(skinServiceGateway.getUserOwnedSkinIds(userId));
        } else {
            ownedSkinIds = Collections.emptySet();
        }

        return items.stream()
                .map(item -> ShopItemResponseDTO.builder()
                        .id(item.getId())
                        .skinItemId(item.getSkinItemId())
                        .code(item.getCode())
                        .name(item.getName())
                        .slot(item.getSlot())
                        .priceRuby(item.getPriceRuby())
                        .imageUrl(item.getImageUrl())
                        .description(item.getDescription())
                        .isOwned(ownedSkinIds.contains(item.getSkinItemId()))
                        .build())
                .collect(Collectors.toList());
    }
}
