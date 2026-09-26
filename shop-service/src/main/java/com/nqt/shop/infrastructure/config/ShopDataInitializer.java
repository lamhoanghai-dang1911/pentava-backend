package com.nqt.shop.infrastructure.config;

import com.nqt.shop.infrastructure.persistence.entity.ShopItemEntity;
import com.nqt.shop.infrastructure.persistence.repository.ShopItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShopDataInitializer implements CommandLineRunner {

    private final ShopItemRepository shopItemRepository;

    @Value("${supabase.url:https://olgtvgfgpaydkopjhern.supabase.co}")
    private String supabaseUrl;

    @Value("${supabase.skin-bucket:skin}")
    private String skinBucket;

    @Override
    public void run(String... args) {
        if (shopItemRepository.count() > 0) {
            log.info("Shop items already initialized, count = {}", shopItemRepository.count());
            return;
        }

        log.info("Initializing default shop items...");
        String baseUrl = supabaseUrl.replaceAll("/+$", "") + "/storage/v1/object/public/" + skinBucket + "/";

        // Lưu ý: skinItemId tương ứng với ID của SkinItemEntity trong skin-service (cat_base là 1)
        List<ShopItemEntity> defaultItems = List.of(
                ShopItemEntity.builder()
                        .skinItemId(2L)
                        .code("yellow_scarf")
                        .name("Khăn quàng vàng")
                        .slot("NECK")
                        .priceRuby(20)
                        .imageUrl(baseUrl + "yellow_scarf.png")
                        .description("Khăn len quàng cổ màu vàng ấm áp")
                        .isForSale(true)
                        .build(),

                ShopItemEntity.builder()
                        .skinItemId(3L)
                        .code("blue_sunglasses")
                        .name("Kính râm xanh")
                        .slot("EYES")
                        .priceRuby(25)
                        .imageUrl(baseUrl + "blue_sunglasses.png")
                        .description("Kính râm viền xanh thời thượng")
                        .isForSale(true)
                        .build(),

                ShopItemEntity.builder()
                        .skinItemId(4L)
                        .code("yellow_sunglasses")
                        .name("Kính râm vàng")
                        .slot("EYES")
                        .priceRuby(25)
                        .imageUrl(baseUrl + "yellow_sunglasses.png")
                        .description("Kính râm viền vàng cá tính")
                        .isForSale(true)
                        .build(),

                ShopItemEntity.builder()
                        .skinItemId(5L)
                        .code("blue_hat")
                        .name("Mũ lưỡi trai xanh")
                        .slot("HEAD")
                        .priceRuby(30)
                        .imageUrl(baseUrl + "blue_hat.png")
                        .description("Chiếc mũ snapback màu xanh năng động")
                        .isForSale(true)
                        .build(),

                ShopItemEntity.builder()
                        .skinItemId(6L)
                        .code("crown")
                        .name("Vương miện hoàng gia")
                        .slot("HEAD")
                        .priceRuby(60)
                        .imageUrl(baseUrl + "crown.png")
                        .description("Vương miện vàng quyền quý dành cho quý tộc mèo")
                        .isForSale(true)
                        .build(),

                ShopItemEntity.builder()
                        .skinItemId(7L)
                        .code("blue_fish")
                        .name("Cá xanh tươi")
                        .slot("ACCESSORY")
                        .priceRuby(15)
                        .imageUrl(baseUrl + "blue_fish.png")
                        .description("Chú cá xanh thơm ngon")
                        .isForSale(true)
                        .build(),

                ShopItemEntity.builder()
                        .skinItemId(8L)
                        .code("pink_fish")
                        .name("Cá hồng ngọt ngào")
                        .slot("ACCESSORY")
                        .priceRuby(15)
                        .imageUrl(baseUrl + "pink_fish.png")
                        .description("Chú cá hồng dễ thương")
                        .isForSale(true)
                        .build(),

                ShopItemEntity.builder()
                        .skinItemId(9L)
                        .code("carton_fish")
                        .name("Thùng cá carton")
                        .slot("ACCESSORY")
                        .priceRuby(35)
                        .imageUrl(baseUrl + "carton_fish.png")
                        .description("Thùng carton đầy ắp cá tươi dành cho mèo sành ăn")
                        .isForSale(true)
                        .build()
        );

        shopItemRepository.saveAll(defaultItems);
        log.info("Initialized {} default shop items successfully.", defaultItems.size());
    }
}
