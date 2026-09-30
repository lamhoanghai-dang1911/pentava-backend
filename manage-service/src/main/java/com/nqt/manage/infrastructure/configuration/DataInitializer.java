package com.nqt.manage.infrastructure.configuration;

import com.nqt.manage.infrastructure.persistence.entity.*;
import com.nqt.manage.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final BannerJpaRepository bannerRepository;
    private final FeatureJpaRepository featureRepository;
    private final TestimonialJpaRepository testimonialRepository;
    private final FaqJpaRepository faqRepository;
    private final PricingPackageJpaRepository pricingRepository;
    private final PlatformMetricJpaRepository metricRepository;
    private final SiteConfigJpaRepository configRepository;

    @Override
    public void run(String... args) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Seed Banners
        if (bannerRepository.count() == 0) {
            log.info("Khởi tạo dữ liệu Banner mặc định cho Landing Page...");
            bannerRepository.saveAll(List.of(
                    BannerEntity.builder()
                            .title("PENTAVA - Thế Giới Kết Nối & Rèn Luyện Thói Quen")
                            .subtitle("Nâng tầm phong cách sống, rèn luyện thói quen mỗi ngày và cùng bạn bè xem phim trong rạp chiếu ảo đỉnh cao.")
                            .imageUrl("https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=1600&auto=format&fit=crop")
                            .ctaText("Tham gia ngay")
                            .ctaLink("/register")
                            .displayOrder(1)
                            .isActive(true)
                            .createdAt(now)
                            .updatedAt(now)
                            .build(),
                    BannerEntity.builder()
                            .title("Penta Cinema - Rạp Chiếu Ảo Cùng Bạn Bè")
                            .subtitle("Thưởng thức các thước phim hấp dẫn cùng hội bạn thân, trò chuyện thời gian thực và nhận thưởng Ruby độc quyền.")
                            .imageUrl("https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?q=80&w=1600&auto=format&fit=crop")
                            .ctaText("Khám phá Cinema")
                            .ctaLink("/cinema")
                            .displayOrder(2)
                            .isActive(true)
                            .createdAt(now)
                            .updatedAt(now)
                            .build()
            ));
        }

        // 2. Seed Features
        if (featureRepository.count() == 0) {
            log.info("Khởi tạo dữ liệu 5 Tính Năng cốt lõi cho Landing Page...");
            featureRepository.saveAll(List.of(
                    FeatureEntity.builder()
                            .title("Check-in Thói Quen Hàng Ngày")
                            .description("Chụp ảnh xác nhận thói quen mỗi ngày, nhận Ruby thưởng và nâng cao tính kỷ luật cá nhân.")
                            .icon("CheckCircle2")
                            .tag("AI CHECK")
                            .previewImageUrl("https://images.unsplash.com/photo-1434493789847-2f02dc6ca35d?q=80&w=600&auto=format&fit=crop")
                            .displayOrder(1)
                            .isActive(true)
                            .createdAt(now)
                            .updatedAt(now)
                            .build(),
                    FeatureEntity.builder()
                            .title("Penta Cinema - Rạp Phim Ảo")
                            .description("Trải nghiệm xem video cùng bạn bè, chia sẻ cảm xúc và tương tác không giới hạn khoảng cách.")
                            .icon("Film")
                            .tag("REALTIME")
                            .previewImageUrl("https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?q=80&w=600&auto=format&fit=crop")
                            .displayOrder(2)
                            .isActive(true)
                            .createdAt(now)
                            .updatedAt(now)
                            .build(),
                    FeatureEntity.builder()
                            .title("Khảo Sát Onboarding Cá Nhân Hóa")
                            .description("Định hình lộ trình thói quen và mục tiêu phù hợp với từng lối sống riêng biệt.")
                            .icon("UserCheck")
                            .tag("PERSONALIZED")
                            .previewImageUrl("https://images.unsplash.com/photo-1516321318423-f06f85e504b3?q=80&w=600&auto=format&fit=crop")
                            .displayOrder(3)
                            .isActive(true)
                            .createdAt(now)
                            .updatedAt(now)
                            .build(),
                    FeatureEntity.builder()
                            .title("Tủ Đồ Skin & Thời Trang Độc Quyền")
                            .description("Hàng trăm bộ trang phục, phụ kiện cá tính giúp bạn nổi bật trong cộng đồng Pentava.")
                            .icon("Shirt")
                            .tag("FASHION")
                            .previewImageUrl("https://images.unsplash.com/photo-1509631179647-0177331693ae?q=80&w=600&auto=format&fit=crop")
                            .displayOrder(4)
                            .isActive(true)
                            .createdAt(now)
                            .updatedAt(now)
                            .build(),
                    FeatureEntity.builder()
                            .title("Cửa Hàng Nạp Ruby Nhanh Chóng")
                            .description("Hỗ trợ thanh toán bảo mật tức thì qua cổng QR chuyển khoản ngân hàng thông minh.")
                            .icon("Coins")
                            .tag("INSTANT")
                            .previewImageUrl("https://images.unsplash.com/photo-1559526324-4b87b5e36e44?q=80&w=600&auto=format&fit=crop")
                            .displayOrder(5)
                            .isActive(true)
                            .createdAt(now)
                            .updatedAt(now)
                            .build()
            ));
        }

        // 3. Seed Testimonials
        if (testimonialRepository.count() == 0) {
            log.info("Khởi tạo dữ liệu Testimonial mặc định...");
            testimonialRepository.saveAll(List.of(
                    TestimonialEntity.builder()
                            .authorName("Minh Hoàng")
                            .authorTitle("Sinh viên Đại học Bách Khoa")
                            .avatarUrl("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=200&auto=format&fit=crop")
                            .content("Từ ngày dùng Pentava, mình duy trì thói quen đọc sách và chạy bộ 30 ngày liên tục mà không hề thấy chán nản!")
                            .rating(5)
                            .isFeatured(true)
                            .displayOrder(1)
                            .isActive(true)
                            .createdAt(now)
                            .updatedAt(now)
                            .build(),
                    TestimonialEntity.builder()
                            .authorName("Ngọc Trâm")
                            .authorTitle("Content Creator")
                            .avatarUrl("https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=200&auto=format&fit=crop")
                            .content("Tính năng Cinema xem chung video với nhóm bạn thân cực vui. Skin avatar lại còn quá nhiều mẫu xinh xắn.")
                            .rating(5)
                            .isFeatured(true)
                            .displayOrder(2)
                            .isActive(true)
                            .createdAt(now)
                            .updatedAt(now)
                            .build()
            ));
        }

        // 4. Seed FAQs
        if (faqRepository.count() == 0) {
            log.info("Khởi tạo dữ liệu FAQ mặc định...");
            faqRepository.saveAll(List.of(
                    FaqEntity.builder()
                            .question("Pentava là gì và dành cho ai?")
                            .answer("Pentava là nền tảng gamification kết hợp rèn luyện thói quen, giải trí và kết nối bạn bè, phù hợp cho tất cả mọi người muốn xây dựng lối sống tích cực.")
                            .category("GENERAL")
                            .displayOrder(1)
                            .isActive(true)
                            .createdAt(now)
                            .updatedAt(now)
                            .build(),
                    FaqEntity.builder()
                            .question("Làm sao để nhận Ruby trong Pentava?")
                            .answer("Bạn có thể nhận Ruby bằng cách hoàn thành check-in thói quen hàng ngày hoặc nạp gói Ruby thông qua Cửa hàng.")
                            .category("SHOP_RUBY")
                            .displayOrder(2)
                            .isActive(true)
                            .createdAt(now)
                            .updatedAt(now)
                            .build(),
                    FaqEntity.builder()
                            .question("Rạp chiếu Penta Cinema có miễn phí không?")
                            .answer("Bạn hoàn toàn có thể tạo phòng và xem phim miễn phí cùng bạn bè với nhiều trải nghiệm thú vị.")
                            .category("CINEMA")
                            .displayOrder(3)
                            .isActive(true)
                            .createdAt(now)
                            .updatedAt(now)
                            .build()
            ));
        }

        // 5. Seed Pricing Packages
        if (pricingRepository.count() == 0) {
            log.info("Khởi tạo dữ liệu Gói nạp Ruby hiển thị trên Landing Page...");
            pricingRepository.saveAll(List.of(
                    PricingPackageEntity.builder()
                            .name("Gói Khởi Đầu")
                            .rubyAmount(100)
                            .bonusRuby(10)
                            .priceVnd(BigDecimal.valueOf(20000))
                            .tag("Tân thủ")
                            .features("110 Ruby tổng cộng, Mở khóa avatar cơ bản, Hỗ trợ 24/7")
                            .ctaText("Nạp ngay")
                            .ctaLink("/shop")
                            .displayOrder(1)
                            .isActive(true)
                            .createdAt(now)
                            .updatedAt(now)
                            .build(),
                    PricingPackageEntity.builder()
                            .name("Gói Thịnh Vượng")
                            .rubyAmount(500)
                            .bonusRuby(100)
                            .priceVnd(BigDecimal.valueOf(100000))
                            .tag("Phổ biến nhất")
                            .features("600 Ruby tổng cộng, Tặng 1 Skin hiếm ngẫu nhiên, Mở phòng Cinema không giới hạn")
                            .ctaText("Chọn gói này")
                            .ctaLink("/shop")
                            .displayOrder(2)
                            .isActive(true)
                            .createdAt(now)
                            .updatedAt(now)
                            .build(),
                    PricingPackageEntity.builder()
                            .name("Gói Hoàng Gia VIP")
                            .rubyAmount(1200)
                            .bonusRuby(300)
                            .priceVnd(BigDecimal.valueOf(250000))
                            .tag("Ưu đãi đặc biệt")
                            .features("1500 Ruby tổng cộng, Huy hiệu thành viên VIP, Quyền ưu tiên tính năng mới")
                            .ctaText("Nâng cấp VIP")
                            .ctaLink("/shop")
                            .displayOrder(3)
                            .isActive(true)
                            .createdAt(now)
                            .updatedAt(now)
                            .build()
            ));
        }

        // 6. Seed Metrics
        if (metricRepository.count() == 0) {
            log.info("Khởi tạo dữ liệu Platform Metrics cho Landing Page...");
            metricRepository.saveAll(List.of(
                    PlatformMetricEntity.builder()
                            .metricKey("ACTIVE_USERS")
                            .label("Người dùng tin cậy")
                            .counterValue(12500L)
                            .suffix("+")
                            .icon("Users")
                            .isAutoCalculated(false)
                            .displayOrder(1)
                            .isActive(true)
                            .updatedAt(now)
                            .build(),
                    PlatformMetricEntity.builder()
                            .metricKey("COMPLETED_HABITS")
                            .label("Lượt check-in thành công")
                            .counterValue(68000L)
                            .suffix("+")
                            .icon("CheckCircle")
                            .isAutoCalculated(false)
                            .displayOrder(2)
                            .isActive(true)
                            .updatedAt(now)
                            .build(),
                    PlatformMetricEntity.builder()
                            .metricKey("CINEMA_HOURS")
                            .label("Giờ xem phim cùng nhau")
                            .counterValue(15400L)
                            .suffix("h+")
                            .icon("Clock")
                            .isAutoCalculated(false)
                            .displayOrder(3)
                            .isActive(true)
                            .updatedAt(now)
                            .build(),
                    PlatformMetricEntity.builder()
                            .metricKey("SATISFACTION_RATE")
                            .label("Tỉ lệ hài lòng")
                            .counterValue(99L)
                            .suffix(".8%")
                            .icon("Smile")
                            .isAutoCalculated(false)
                            .displayOrder(4)
                            .isActive(true)
                            .updatedAt(now)
                            .build()
            ));
        }

        // 7. Seed Site Configs
        if (configRepository.count() == 0) {
            log.info("Khởi tạo cấu hình Site Configs mặc định...");
            configRepository.saveAll(List.of(
                    SiteConfigEntity.builder()
                            .configKey("HOTLINE")
                            .configValue("1900 8888")
                            .groupName("CONTACT")
                            .description("Số điện thoại hỗ trợ")
                            .isPublic(true)
                            .updatedAt(now)
                            .build(),
                    SiteConfigEntity.builder()
                            .configKey("EMAIL_SUPPORT")
                            .configValue("support@pentava.online")
                            .groupName("CONTACT")
                            .description("Email chăm sóc khách hàng")
                            .isPublic(true)
                            .updatedAt(now)
                            .build(),
                    SiteConfigEntity.builder()
                            .configKey("FACEBOOK_URL")
                            .configValue("https://facebook.com/pentava")
                            .groupName("SOCIAL")
                            .description("Trang Fanpage Facebook chính thức")
                            .isPublic(true)
                            .updatedAt(now)
                            .build(),
                    SiteConfigEntity.builder()
                            .configKey("DISCORD_URL")
                            .configValue("https://discord.gg/pentava")
                            .groupName("SOCIAL")
                            .description("Kênh Discord cộng đồng")
                            .isPublic(true)
                            .updatedAt(now)
                            .build(),
                    SiteConfigEntity.builder()
                            .configKey("TERMS_OF_SERVICE")
                            .configValue("Điều khoản sử dụng dịch vụ PENTAVA nhằm bảo vệ quyền lợi người dùng và duy trì cộng đồng văn minh.")
                            .groupName("LEGAL")
                            .description("Điều khoản dịch vụ")
                            .isPublic(true)
                            .updatedAt(now)
                            .build(),
                    SiteConfigEntity.builder()
                            .configKey("PRIVACY_POLICY")
                            .configValue("PENTAVA cam kết bảo mật tuyệt đối thông tin cá nhân và dữ liệu thói quen của người dùng.")
                            .groupName("LEGAL")
                            .description("Chính sách bảo mật")
                            .isPublic(true)
                            .updatedAt(now)
                            .build()
            ));
        }
    }
}
