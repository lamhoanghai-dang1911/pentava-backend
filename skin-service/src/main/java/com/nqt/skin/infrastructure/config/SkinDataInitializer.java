package com.nqt.skin.infrastructure.config;

import com.nqt.skin.domain.model.Slot;
import com.nqt.skin.infrastructure.persistence.entity.SkinItemEntity;
import com.nqt.skin.infrastructure.persistence.repository.SkinItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SkinDataInitializer implements CommandLineRunner {

    private final SkinItemRepository skinItemRepository;

    @Value("${supabase.url:https://olgtvgfgpaydkopjhern.supabase.co}")
    private String supabaseUrl;

    @Value("${supabase.skin-bucket:skin}")
    private String skinBucket;

    @Override
    public void run(String... args) {
        if (skinItemRepository.count() > 0) {
            log.info("Skin items already initialized, count = {}", skinItemRepository.count());
            return;
        }

        log.info("Initializing default skin items from Supabase bucket '{}'...", skinBucket);
        String baseUrl = supabaseUrl.replaceAll("/+$", "") + "/storage/v1/object/public/" + skinBucket + "/";

        List<SkinItemEntity> defaultSkins = List.of(
                // 1. Base mèo (Layer 0 - luôn là nền tảng)
                SkinItemEntity.builder()
                        .code("cat_base")
                        .name("Mèo cơ bản")
                        .slot(Slot.BASE)
                        .layerOrder(0)
                        .imageFileName("cat_base.png")
                        .imageUrl(baseUrl + "cat_base.png")
                        .priceRuby(0)
                        .description("Nhân vật mèo nguyên bản, luôn luôn là lớp hiển thị gốc")
                        .isActive(true)
                        .build(),

                // 2. Cổ (Layer 1)
                SkinItemEntity.builder()
                        .code("yellow_scarf")
                        .name("Khăn quàng vàng")
                        .slot(Slot.NECK)
                        .layerOrder(1)
                        .imageFileName("yellow_scarf.png")
                        .imageUrl(baseUrl + "yellow_scarf.png")
                        .priceRuby(20)
                        .description("Khăn len quàng cổ màu vàng ấm áp")
                        .isActive(true)
                        .build(),

                // 3. Mắt (Layer 2)
                SkinItemEntity.builder()
                        .code("blue_sunglasses")
                        .name("Kính râm xanh")
                        .slot(Slot.EYES)
                        .layerOrder(2)
                        .imageFileName("blue_sunglasses.png")
                        .imageUrl(baseUrl + "blue_sunglasses.png")
                        .priceRuby(25)
                        .description("Kính râm viền xanh thời thượng")
                        .isActive(true)
                        .build(),

                SkinItemEntity.builder()
                        .code("yellow_sunglasses")
                        .name("Kính râm vàng")
                        .slot(Slot.EYES)
                        .layerOrder(2)
                        .imageFileName("yellow_sunglasses.png")
                        .imageUrl(baseUrl + "yellow_sunglasses.png")
                        .priceRuby(25)
                        .description("Kính râm viền vàng cá tính")
                        .isActive(true)
                        .build(),

                // 4. Đầu (Layer 3)
                SkinItemEntity.builder()
                        .code("blue_hat")
                        .name("Mũ lưỡi trai xanh")
                        .slot(Slot.HEAD)
                        .layerOrder(3)
                        .imageFileName("blue_hat.png")
                        .imageUrl(baseUrl + "blue_hat.png")
                        .priceRuby(30)
                        .description("Chiếc mũ snapback màu xanh năng động")
                        .isActive(true)
                        .build(),

                SkinItemEntity.builder()
                        .code("crown")
                        .name("Vương miện hoàng gia")
                        .slot(Slot.HEAD)
                        .layerOrder(3)
                        .imageFileName("crown.png")
                        .imageUrl(baseUrl + "crown.png")
                        .priceRuby(60)
                        .description("Vương miện vàng quyền quý dành cho quý tộc mèo")
                        .isActive(true)
                        .build(),

                // 5. Phụ kiện / Cầm tay (Layer 4)
                SkinItemEntity.builder()
                        .code("blue_fish")
                        .name("Cá xanh tươi")
                        .slot(Slot.ACCESSORY)
                        .layerOrder(4)
                        .imageFileName("blue_fish.png")
                        .imageUrl(baseUrl + "blue_fish.png")
                        .priceRuby(15)
                        .description("Chú cá xanh thơm ngon")
                        .isActive(true)
                        .build(),

                SkinItemEntity.builder()
                        .code("pink_fish")
                        .name("Cá hồng ngọt ngào")
                        .slot(Slot.ACCESSORY)
                        .layerOrder(4)
                        .imageFileName("pink_fish.png")
                        .imageUrl(baseUrl + "pink_fish.png")
                        .priceRuby(15)
                        .description("Chú cá hồng dễ thương")
                        .isActive(true)
                        .build(),

                SkinItemEntity.builder()
                        .code("carton_fish")
                        .name("Thùng cá carton")
                        .slot(Slot.ACCESSORY)
                        .layerOrder(4)
                        .imageFileName("carton_fish.png")
                        .imageUrl(baseUrl + "carton_fish.png")
                        .priceRuby(35)
                        .description("Thùng carton đầy ắp cá tươi dành cho mèo sành ăn")
                        .isActive(true)
                        .build()
        );

        skinItemRepository.saveAll(defaultSkins);
        log.info("Initialized {} default skin items successfully.", defaultSkins.size());
    }
}
