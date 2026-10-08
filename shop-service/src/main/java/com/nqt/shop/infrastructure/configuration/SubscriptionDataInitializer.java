package com.nqt.shop.infrastructure.configuration;

import com.nqt.shop.infrastructure.persistence.entity.PlanFeatureMappingEntity;
import com.nqt.shop.infrastructure.persistence.entity.SubscriptionFeatureEntity;
import com.nqt.shop.infrastructure.persistence.entity.SubscriptionPlanEntity;
import com.nqt.shop.infrastructure.persistence.repository.PlanFeatureMappingRepository;
import com.nqt.shop.infrastructure.persistence.repository.SubscriptionFeatureRepository;
import com.nqt.shop.infrastructure.persistence.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionDataInitializer implements CommandLineRunner {

    private final SubscriptionFeatureRepository featureRepository;
    private final SubscriptionPlanRepository planRepository;
    private final PlanFeatureMappingRepository mappingRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Bắt đầu kiểm tra và khởi tạo dữ liệu Gói cước (Subscription Plans & Features)...");

        // 1. Khởi tạo 6 Tính năng hệ thống nếu chưa có
        initFeatures();

        // 2. Khởi tạo 3 Gói cước (BASIC, GOLD, PREMIUM) nếu chưa có
        initPlans();

        // 3. Khởi tạo cấu hình phân bổ tính năng ban đầu cho từng gói
        initDefaultMappings();

        log.info("Hoàn tất khởi tạo dữ liệu Gói cước (Subscription Plans & Features).");
    }

    private void initFeatures() {
        if (featureRepository.count() > 0) {
            log.info("Danh mục tính năng (subscription_features) đã có dữ liệu, bỏ qua bước khởi tạo tính năng.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<SubscriptionFeatureEntity> features = List.of(
                SubscriptionFeatureEntity.builder()
                        .featureCode("STORAGE_EXPANSION")
                        .name("Tăng dung lượng lưu trữ")
                        .description(
                                "Tăng khả năng lưu trữ dữ liệu của riêng user để lưu trữ hình ảnh và video Penta-Cinema")
                        .defaultValue("5GB")
                        .unit("GB")
                        .displayOrder(1)
                        .createdAt(now)
                        .updatedAt(now)
                        .build(),

                SubscriptionFeatureEntity.builder()
                        .featureCode("STREAK_PROTECTION")
                        .name("Streak Protection (Đóng băng tiến trình)")
                        .description("Đóng băng tiến trình check-in thói quen cho những ngày bận rộn")
                        .defaultValue("2")
                        .unit("lần/tháng")
                        .displayOrder(2)
                        .createdAt(now)
                        .updatedAt(now)
                        .build(),

                SubscriptionFeatureEntity.builder()
                        .featureCode("MULTI_ANGLE_TEMPLATES")
                        .name("Multi-Angle & Dynamic Templates")
                        .description("Tạo video ghép nhiều hành trình Penta-Cinema với các mẫu hiệu ứng động")
                        .defaultValue("true")
                        .unit("boolean")
                        .displayOrder(3)
                        .createdAt(now)
                        .updatedAt(now)
                        .build(),

                SubscriptionFeatureEntity.builder()
                        .featureCode("CUSTOM_ROADMAP")
                        .name("Tự thiết kế & Public lộ trình")
                        .description(
                                "Cho người dùng tự thiết kế lộ trình phát triển bản thân và chia sẻ công khai lên cộng đồng")
                        .defaultValue("true")
                        .unit("boolean")
                        .displayOrder(4)
                        .createdAt(now)
                        .updatedAt(now)
                        .build(),

                SubscriptionFeatureEntity.builder()
                        .featureCode("UNLOCK_ALL_SKINS")
                        .name("Full trang phục cửa hàng")
                        .description("Được toàn quyền sử dụng tất cả các trang phục có sẵn trong cửa hàng")
                        .defaultValue("true")
                        .unit("boolean")
                        .displayOrder(5)
                        .createdAt(now)
                        .updatedAt(now)
                        .build(),

                SubscriptionFeatureEntity.builder()
                        .featureCode("RUBY_BONUS")
                        .name("Tặng Ruby khi mua gói")
                        .description("Tặng Ruby vào ví ngay khi kích hoạt gói cước thành công")
                        .defaultValue("50")
                        .unit("Ruby")
                        .displayOrder(6)
                        .createdAt(now)
                        .updatedAt(now)
                        .build());

        featureRepository.saveAll(features);
        log.info("Đã tạo thành công 6 tính năng mặc định trong subscription_features.");
    }

    private void initPlans() {
        if (planRepository.count() > 0) {
            log.info("Danh mục gói cước (subscription_plans) đã có dữ liệu, cập nhật lại description thành null.");
            List<SubscriptionPlanEntity> existingPlans = planRepository.findAll();
            boolean updated = false;
            for (SubscriptionPlanEntity p : existingPlans) {
                if (p.getDescription() != null) {
                    p.setDescription(null);
                    updated = true;
                }
            }
            if (updated) {
                planRepository.saveAll(existingPlans);
            }
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<SubscriptionPlanEntity> plans = List.of(
                SubscriptionPlanEntity.builder()
                        .code("BASIC")
                        .name("Gói Basic")
                        .priceVnd(39000L)
                        .durationDays(30)
                        .description(null)
                        .badge(null)
                        .isActive(true)
                        .displayOrder(1)
                        .createdAt(now)
                        .updatedAt(now)
                        .build(),

                SubscriptionPlanEntity.builder()
                        .code("GOLD")
                        .name("Gói Gold")
                        .priceVnd(59000L)
                        .durationDays(30)
                        .description(null)
                        .badge("Phổ biến nhất")
                        .isActive(true)
                        .displayOrder(2)
                        .createdAt(now)
                        .updatedAt(now)
                        .build(),

                SubscriptionPlanEntity.builder()
                        .code("PREMIUM")
                        .name("Gói Premium")
                        .priceVnd(149000L)
                        .durationDays(30)
                        .description(null)
                        .badge("VIP")
                        .isActive(true)
                        .displayOrder(3)
                        .createdAt(now)
                        .updatedAt(now)
                        .build());

        planRepository.saveAll(plans);
        log.info("Đã tạo thành công 3 gói cước mặc định: BASIC (39k), GOLD (59k), PREMIUM (149k).");
    }

    private void initDefaultMappings() {
        if (mappingRepository.count() > 0) {
            log.info("Cấu hình mapping tính năng gói cước đã tồn tại, giữ nguyên cấu hình hiện tại.");
            return;
        }

        List<SubscriptionPlanEntity> plans = planRepository.findAll();
        List<SubscriptionFeatureEntity> features = featureRepository.findAll();

        LocalDateTime now = LocalDateTime.now();

        for (SubscriptionPlanEntity plan : plans) {
            for (SubscriptionFeatureEntity feature : features) {
                boolean enabled = false;
                String paramVal = feature.getDefaultValue();
                String displayLabel = feature.getName();

                // Cấu hình ban đầu dạng Modular (Admin có thể tùy ý sửa đổi qua giao diện bất
                // cứ lúc nào)
                switch (plan.getCode().toUpperCase()) {
                    case "BASIC":
                        if ("STORAGE_EXPANSION".equalsIgnoreCase(feature.getFeatureCode())) {
                            enabled = true;
                            paramVal = "5GB";
                            displayLabel = "Tăng thêm 5GB lưu trữ hình ảnh & video Penta-Cinema";
                        } else if ("RUBY_BONUS".equalsIgnoreCase(feature.getFeatureCode())) {
                            enabled = true;
                            paramVal = "20";
                            displayLabel = "Tặng ngay 20 Ruby vào ví tài khoản";
                        }
                        break;

                    case "GOLD":
                        if ("STREAK_PROTECTION".equalsIgnoreCase(feature.getFeatureCode())) {
                            enabled = true;
                            paramVal = "2";
                            displayLabel = "Đóng băng tiến trình (Streak Protection) 2 lần/tháng cho những ngày bận rộn";
                        } else if ("CUSTOM_ROADMAP".equalsIgnoreCase(feature.getFeatureCode())) {
                            enabled = true;
                            paramVal = "true";
                            displayLabel = "Tự do thiết kế lộ trình phát triển và công khai (public) ra cộng đồng";
                        } else if ("RUBY_BONUS".equalsIgnoreCase(feature.getFeatureCode())) {
                            enabled = true;
                            paramVal = "50";
                            displayLabel = "Tặng ngay 50 Ruby vào ví tài khoản";
                        }
                        break;

                    case "PREMIUM":
                        if ("MULTI_ANGLE_TEMPLATES".equalsIgnoreCase(feature.getFeatureCode())) {
                            enabled = true;
                            paramVal = "true";
                            displayLabel = "Mở khóa mẫu video Multi-Angle & Dynamic Templates ghép nhiều hành trình";
                        } else if ("UNLOCK_ALL_SKINS".equalsIgnoreCase(feature.getFeatureCode())) {
                            enabled = true;
                            paramVal = "true";
                            displayLabel = "Được toàn quyền sử dụng full tất cả trang phục có sẵn trong cửa hàng";
                        } else if ("STORAGE_EXPANSION".equalsIgnoreCase(feature.getFeatureCode())) {
                            enabled = true;
                            paramVal = "50GB";
                            displayLabel = "Tăng thêm 50GB lưu trữ không giới hạn hình ảnh & video Penta-Cinema";
                        } else if ("RUBY_BONUS".equalsIgnoreCase(feature.getFeatureCode())) {
                            enabled = true;
                            paramVal = "150";
                            displayLabel = "Tặng ngay 150 Ruby vào ví tài khoản";
                        }
                        break;
                }

                PlanFeatureMappingEntity mapping = PlanFeatureMappingEntity.builder()
                        .plan(plan)
                        .feature(feature)
                        .isEnabled(enabled)
                        .paramValue(paramVal)
                        .displayLabel(displayLabel)
                        .updatedAt(now)
                        .build();

                mappingRepository.save(mapping);
            }
        }

        log.info("Đã khởi tạo thành công ma trận tính năng mặc định cho các gói cước.");
    }
}
