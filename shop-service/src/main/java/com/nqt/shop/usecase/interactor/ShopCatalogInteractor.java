package com.nqt.shop.usecase.interactor;

import com.nqt.shop.infrastructure.persistence.entity.ShopItemEntity;
import com.nqt.shop.infrastructure.persistence.repository.ShopItemRepository;
import com.nqt.shop.usecase.boundary.ShopCatalogInputPort;
import com.nqt.shop.usecase.boundary.SkinServiceGateway;
import com.nqt.shop.usecase.dto.ShopItemResponseDTO;
import com.nqt.shop.usecase.dto.ShopThumbnailResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${supabase.url:https://olgtvgfgpaydkopjhern.supabase.co}")
    private String supabaseUrl;

    @Value("${supabase.thumbnail-bucket:shop_thumbnail}")
    private String thumbnailBucket;

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

        String itemBackgroundUrl = getItemBackgroundUrl();

        return items.stream()
                .map(item -> {
                    String thumbnailUrl = resolveThumbnailUrl(item);
                    return ShopItemResponseDTO.builder()
                            .id(item.getId())
                            .skinItemId(item.getSkinItemId())
                            .code(item.getCode())
                            .name(item.getName())
                            .slot(item.getSlot())
                            .priceRuby(item.getPriceRuby())
                            .imageUrl(item.getImageUrl())
                            .itemBackgroundUrl(itemBackgroundUrl)
                            .thumbnailUrl(thumbnailUrl)
                            .thumbnailLayers(buildThumbnailLayers(thumbnailUrl))
                            .description(item.getDescription())
                            .isOwned(ownedSkinIds.contains(item.getSkinItemId()))
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopThumbnailResponseDTO> getShopThumbnails() {
        List<ShopItemEntity> items = shopItemRepository.findByIsForSaleTrue();
        String itemBackgroundUrl = getItemBackgroundUrl();

        return items.stream()
                .map(item -> {
                    String thumbnailUrl = resolveThumbnailUrl(item);
                    return ShopThumbnailResponseDTO.builder()
                            .shopItemId(item.getId())
                            .skinItemId(item.getSkinItemId())
                            .code(item.getCode())
                            .name(item.getName())
                            .slot(item.getSlot())
                            .itemBackgroundUrl(itemBackgroundUrl)
                            .thumbnailUrl(thumbnailUrl)
                            .thumbnailLayers(buildThumbnailLayers(thumbnailUrl))
                            .build();
                })
                .collect(Collectors.toList());
    }

    private String resolveThumbnailUrl(ShopItemEntity item) {
        String url = item.getThumbnailUrl();
        if (url != null && url.contains("crown_background.png")) {
            return url.replace("crown_background.png", "crown_background_replace.png");
        }
        if ("crown".equalsIgnoreCase(item.getCode()) && (url == null || url.isBlank())) {
            return supabaseUrl.replaceAll("/+$", "") + "/storage/v1/object/public/" + thumbnailBucket + "/crown_background_replace.png";
        }
        return url;
    }

    private String getItemBackgroundUrl() {
        return supabaseUrl.replaceAll("/+$", "") + "/storage/v1/object/public/" + thumbnailBucket + "/item_background.png";
    }

    private List<String> buildThumbnailLayers(String thumbnailUrl) {
        String bg = getItemBackgroundUrl();
        if (thumbnailUrl != null && !thumbnailUrl.isBlank()) {
            return List.of(bg, thumbnailUrl);
        }
        return List.of(bg);
    }
}
