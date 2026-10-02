package com.nqt.shop.usecase.interactor;

import com.nqt.shop.infrastructure.persistence.entity.ShopItemEntity;
import com.nqt.shop.infrastructure.persistence.repository.ShopItemRepository;
import com.nqt.shop.usecase.boundary.SkinServiceGateway;
import com.nqt.shop.usecase.dto.ShopItemResponseDTO;
import com.nqt.shop.usecase.dto.ShopThumbnailResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopCatalogInteractorTest {

    @Mock
    private ShopItemRepository shopItemRepository;

    @Mock
    private SkinServiceGateway skinServiceGateway;

    @InjectMocks
    private ShopCatalogInteractor shopCatalogInteractor;

    private static final String SUPABASE_URL = "https://olgtvgfgpaydkopjhern.supabase.co";
    private static final String THUMBNAIL_BUCKET = "shop_thumbnail";
    private static final String EXPECTED_ITEM_BG_URL = SUPABASE_URL + "/storage/v1/object/public/" + THUMBNAIL_BUCKET + "/item_background.png";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(shopCatalogInteractor, "supabaseUrl", SUPABASE_URL);
        ReflectionTestUtils.setField(shopCatalogInteractor, "thumbnailBucket", THUMBNAIL_BUCKET);
    }

    @Test
    void testGetCatalogIncludesThumbnailsAndOrderedLayers() {
        ShopItemEntity item = ShopItemEntity.builder()
                .id(1L)
                .skinItemId(8L)
                .code("pink_fish")
                .name("Cá hồng ngọt ngào")
                .slot("ACCESSORY")
                .priceRuby(15)
                .imageUrl(SUPABASE_URL + "/storage/v1/object/public/skin/pink_fish.png")
                .thumbnailUrl(SUPABASE_URL + "/storage/v1/object/public/shop_thumbnail/pink_fish_background.png")
                .description("Chú cá hồng dễ thương")
                .isForSale(true)
                .build();

        when(shopItemRepository.findByIsForSaleTrue()).thenReturn(List.of(item));
        when(skinServiceGateway.getUserOwnedSkinIds(1L)).thenReturn(List.of(8L));

        List<ShopItemResponseDTO> catalog = shopCatalogInteractor.getCatalog(1L);

        assertEquals(1, catalog.size());
        ShopItemResponseDTO dto = catalog.get(0);
        assertEquals("pink_fish", dto.getCode());
        assertTrue(dto.isOwned());
        assertEquals(EXPECTED_ITEM_BG_URL, dto.getItemBackgroundUrl());
        assertEquals(SUPABASE_URL + "/storage/v1/object/public/shop_thumbnail/pink_fish_background.png", dto.getThumbnailUrl());

        // Verify layer rendering order: layer 0 is item_background, layer 1 is item thumbnail
        assertNotNull(dto.getThumbnailLayers());
        assertEquals(2, dto.getThumbnailLayers().size());
        assertEquals(EXPECTED_ITEM_BG_URL, dto.getThumbnailLayers().get(0));
        assertEquals(SUPABASE_URL + "/storage/v1/object/public/shop_thumbnail/pink_fish_background.png", dto.getThumbnailLayers().get(1));
    }

    @Test
    void testGetShopThumbnails() {
        ShopItemEntity item1 = ShopItemEntity.builder()
                .id(1L)
                .skinItemId(2L)
                .code("yellow_scarf")
                .name("Khăn quàng vàng")
                .slot("NECK")
                .thumbnailUrl(SUPABASE_URL + "/storage/v1/object/public/shop_thumbnail/yellow_scarf_background.png")
                .isForSale(true)
                .build();

        when(shopItemRepository.findByIsForSaleTrue()).thenReturn(List.of(item1));

        List<ShopThumbnailResponseDTO> thumbnails = shopCatalogInteractor.getShopThumbnails();

        assertEquals(1, thumbnails.size());
        ShopThumbnailResponseDTO dto = thumbnails.get(0);
        assertEquals("yellow_scarf", dto.getCode());
        assertEquals(EXPECTED_ITEM_BG_URL, dto.getItemBackgroundUrl());
        assertEquals(SUPABASE_URL + "/storage/v1/object/public/shop_thumbnail/yellow_scarf_background.png", dto.getThumbnailUrl());
        assertEquals(2, dto.getThumbnailLayers().size());
        assertEquals(EXPECTED_ITEM_BG_URL, dto.getThumbnailLayers().get(0));
        assertEquals(SUPABASE_URL + "/storage/v1/object/public/shop_thumbnail/yellow_scarf_background.png", dto.getThumbnailLayers().get(1));
    }
}
