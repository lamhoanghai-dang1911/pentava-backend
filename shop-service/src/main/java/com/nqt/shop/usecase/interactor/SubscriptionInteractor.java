package com.nqt.shop.usecase.interactor;

import com.nqt.shop.domain.model.TopupStatus;
import com.nqt.shop.infrastructure.persistence.entity.*;
import com.nqt.shop.infrastructure.persistence.repository.*;
import com.nqt.shop.usecase.boundary.SubscriptionInputPort;
import com.nqt.shop.usecase.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionInteractor implements SubscriptionInputPort {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionFeatureRepository subscriptionFeatureRepository;
    private final PlanFeatureMappingRepository planFeatureMappingRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionOrderRepository subscriptionOrderRepository;
    private final UserWalletRepository userWalletRepository;

    @Value("${sepay.bank-code:MBBank}")
    private String sepayBankCode;

    @Value("${sepay.account-number:0123456789}")
    private String sepayAccountNumber;

    @Value("${sepay.account-name:PENTAVA}")
    private String sepayAccountName;

    private static final SecureRandom RANDOM = new SecureRandom();

    // ==========================================
    // 1. CLIENT / USER ENDPOINTS
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponseDTO> getActivePlans() {
        List<SubscriptionPlanEntity> activePlans = subscriptionPlanRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
        return activePlans.stream()
                .map(this::mapToPlanResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserSubscriptionDetailResponseDTO getMySubscription(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        Optional<UserSubscriptionEntity> activeSubOpt = userSubscriptionRepository
                .findFirstByUserIdAndStatusAndEndDateAfterOrderByEndDateDesc(userId, "ACTIVE", now);

        if (activeSubOpt.isEmpty()) {
            return UserSubscriptionDetailResponseDTO.builder()
                    .userId(userId)
                    .hasActiveSubscription(false)
                    .activeFeatures(Collections.emptyList())
                    .build();
        }

        UserSubscriptionEntity sub = activeSubOpt.get();
        SubscriptionPlanEntity plan = sub.getPlan();
        long daysRemaining = Math.max(0, Duration.between(now, sub.getEndDate()).toDays());

        List<PlanFeatureMappingEntity> enabledMappings = planFeatureMappingRepository
                .findByPlanIdAndIsEnabledTrue(plan.getId());

        List<PlanFeatureItemDTO> activeFeatures = enabledMappings.stream()
                .map(this::mapToFeatureItemDTO)
                .collect(Collectors.toList());

        return UserSubscriptionDetailResponseDTO.builder()
                .userId(userId)
                .hasActiveSubscription(true)
                .planCode(plan.getCode())
                .planName(plan.getName())
                .badge(plan.getBadge())
                .startDate(sub.getStartDate())
                .endDate(sub.getEndDate())
                .daysRemaining(daysRemaining)
                .activeFeatures(activeFeatures)
                .build();
    }

    @Override
    @Transactional
    public SubscriptionPurchaseInitResponseDTO initPurchase(Long userId, SubscriptionPurchaseInitRequestDTO request) {
        if (request == null || request.getPlanCode() == null || request.getPlanCode().isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn gói cước cần mua");
        }

        SubscriptionPlanEntity plan = subscriptionPlanRepository.findByCodeIgnoreCase(request.getPlanCode().trim())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy gói cước với mã: " + request.getPlanCode()));

        if (!plan.isActive()) {
            throw new IllegalStateException("Gói cước '" + plan.getName() + "' hiện đang tạm ngừng mở bán");
        }

        // Sinh mã giao dịch PENTAVA + 6 chữ số ngẫu nhiên
        String txCode = "PENTAVA" + (100000 + RANDOM.nextInt(900000));

        SubscriptionOrderEntity order = SubscriptionOrderEntity.builder()
                .userId(userId)
                .plan(plan)
                .transactionCode(txCode)
                .amountVnd(plan.getPriceVnd())
                .status(TopupStatus.PENDING)
                .paymentMethod("SEPAY")
                .bankCode(sepayBankCode)
                .accountNumber(sepayAccountNumber)
                .accountName(sepayAccountName)
                .createdAt(LocalDateTime.now())
                .build();

        // Tạo VietQR SePay URL
        String qrUrl = String.format(
                "https://qr.sepay.vn/img?acc=%s&bank=%s&amount=%d&des=%s&template=compact",
                URLEncoder.encode(sepayAccountNumber, StandardCharsets.UTF_8),
                URLEncoder.encode(sepayBankCode, StandardCharsets.UTF_8),
                plan.getPriceVnd(),
                URLEncoder.encode(txCode, StandardCharsets.UTF_8)
        );

        order.setQrCodeUrl(qrUrl);
        subscriptionOrderRepository.save(order);

        log.info("Initialized subscription purchase order for user {}: txCode={}, plan={}, amountVnd={}",
                userId, txCode, plan.getCode(), plan.getPriceVnd());

        return SubscriptionPurchaseInitResponseDTO.builder()
                .transactionCode(txCode)
                .amountVnd(plan.getPriceVnd())
                .planCode(plan.getCode())
                .planName(plan.getName())
                .durationDays(plan.getDurationDays())
                .description("Đăng ký " + plan.getName() + " (30 ngày)")
                .bankCode(sepayBankCode)
                .accountNumber(sepayAccountNumber)
                .accountName(sepayAccountName)
                .transferContent(txCode)
                .qrCodeUrl(qrUrl)
                .status(TopupStatus.PENDING)
                .createdAt(order.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void mockConfirmPurchase(String transactionCode) {
        if (transactionCode == null || transactionCode.isBlank()) {
            throw new IllegalArgumentException("Mã giao dịch không hợp lệ");
        }

        SubscriptionOrderEntity order = subscriptionOrderRepository.findByTransactionCode(transactionCode.trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với mã giao dịch: " + transactionCode));

        if (order.getStatus() == TopupStatus.SUCCESS) {
            log.info("Đơn mua gói {} đã được kích hoạt trước đó", transactionCode);
            return;
        }

        processOrderActivation(order, "MOCK_DEV_CONFIRM");
    }

    @Override
    @Transactional
    public void processSubscriptionOrderPayment(SubscriptionOrderEntity order, SepayWebhookPayloadDTO payload) {
        if (order.getStatus() == TopupStatus.SUCCESS) {
            log.info("Giao dịch mua gói {} đã được xử lý trước đó", order.getTransactionCode());
            return;
        }

        long transferred = payload.getTransferAmount() != null ? payload.getTransferAmount() : 0L;
        if (transferred < order.getAmountVnd()) {
            log.warn("Số tiền chuyển ({} VNĐ) nhỏ hơn giá gói ({} VNĐ) cho mã {}",
                    transferred, order.getAmountVnd(), order.getTransactionCode());
            return;
        }

        processOrderActivation(order, payload.getReferenceCode());
    }

    private void processOrderActivation(SubscriptionOrderEntity order, String referenceCode) {
        LocalDateTime now = LocalDateTime.now();
        order.setStatus(TopupStatus.SUCCESS);
        order.setCompletedAt(now);
        order.setSepayReferenceCode(referenceCode);
        subscriptionOrderRepository.save(order);

        SubscriptionPlanEntity plan = order.getPlan();
        Long userId = order.getUserId();

        // 1. Kích hoạt / gia hạn gói cho người dùng
        Optional<UserSubscriptionEntity> existingActiveSub = userSubscriptionRepository
                .findFirstByUserIdAndStatusAndEndDateAfterOrderByEndDateDesc(userId, "ACTIVE", now);

        if (existingActiveSub.isPresent()) {
            UserSubscriptionEntity existing = existingActiveSub.get();
            if (existing.getPlan().getId().equals(plan.getId())) {
                // Gia hạn thêm vào hạn sử dụng hiện tại
                existing.setEndDate(existing.getEndDate().plusDays(plan.getDurationDays()));
                existing.setLastTransactionCode(order.getTransactionCode());
                existing.setUpdatedAt(now);
                userSubscriptionRepository.save(existing);
                log.info("Gia hạn thành công gói {} cho user {}. Hạn mới: {}", plan.getCode(), userId, existing.getEndDate());
            } else {
                // Đổi gói (Upgrade / Change plan): Đánh dấu gói cũ đã được nâng cấp, kích hoạt gói mới
                existing.setStatus("UPGRADED");
                existing.setUpdatedAt(now);
                userSubscriptionRepository.save(existing);

                UserSubscriptionEntity newSub = UserSubscriptionEntity.builder()
                        .userId(userId)
                        .plan(plan)
                        .startDate(now)
                        .endDate(now.plusDays(plan.getDurationDays()))
                        .status("ACTIVE")
                        .lastTransactionCode(order.getTransactionCode())
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
                userSubscriptionRepository.save(newSub);
                log.info("Nâng cấp gói thành {} cho user {}. Hạn mới: {}", plan.getCode(), userId, newSub.getEndDate());
            }
        } else {
            // Đăng ký mới
            UserSubscriptionEntity newSub = UserSubscriptionEntity.builder()
                    .userId(userId)
                    .plan(plan)
                    .startDate(now)
                    .endDate(now.plusDays(plan.getDurationDays()))
                    .status("ACTIVE")
                    .lastTransactionCode(order.getTransactionCode())
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            userSubscriptionRepository.save(newSub);
            log.info("Đăng ký mới thành công gói {} cho user {}. Hạn mới: {}", plan.getCode(), userId, newSub.getEndDate());
        }

        // 2. Kiểm tra nếu gói có tính năng RUBY_BONUS thì cộng ngay Ruby vào ví
        Optional<PlanFeatureMappingEntity> rubyBonusMapping = planFeatureMappingRepository
                .findByPlanIdAndFeatureFeatureCodeIgnoreCase(plan.getId(), "RUBY_BONUS");

        if (rubyBonusMapping.isPresent() && rubyBonusMapping.get().isEnabled()) {
            String paramVal = rubyBonusMapping.get().getParamValue();
            long bonusRuby = parseLongOrDefault(paramVal, 0L);
            if (bonusRuby > 0) {
                creditRubyToUser(userId, bonusRuby);
                log.info("Tự động cộng thưởng {} Ruby vào ví của user {} theo gói {}", bonusRuby, userId, plan.getCode());
            }
        }
    }

    private void creditRubyToUser(Long userId, long rubyAmount) {
        if (rubyAmount <= 0) return;
        UserWalletEntity wallet = userWalletRepository.findByUserId(userId)
                .orElseGet(() -> UserWalletEntity.builder()
                        .userId(userId)
                        .rubyBalance(0L)
                        .updatedAt(LocalDateTime.now())
                        .build());

        wallet.setRubyBalance(wallet.getRubyBalance() + rubyAmount);
        wallet.setUpdatedAt(LocalDateTime.now());
        userWalletRepository.save(wallet);
    }

    // ==========================================
    // 2. ENTITLEMENT CHECKING (CHO CÁC MICROSERVICES)
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public UserEntitlementCheckResponseDTO checkUserEntitlement(Long userId, String featureCode) {
        if (userId == null || featureCode == null || featureCode.isBlank()) {
            return UserEntitlementCheckResponseDTO.builder()
                    .userId(userId)
                    .featureCode(featureCode)
                    .hasEntitlement(false)
                    .build();
        }

        LocalDateTime now = LocalDateTime.now();
        Optional<UserSubscriptionEntity> activeSub = userSubscriptionRepository
                .findFirstByUserIdAndStatusAndEndDateAfterOrderByEndDateDesc(userId, "ACTIVE", now);

        if (activeSub.isEmpty()) {
            return UserEntitlementCheckResponseDTO.builder()
                    .userId(userId)
                    .featureCode(featureCode)
                    .hasEntitlement(false)
                    .build();
        }

        UserSubscriptionEntity sub = activeSub.get();
        Optional<PlanFeatureMappingEntity> mappingOpt = planFeatureMappingRepository
                .findByPlanIdAndFeatureFeatureCodeIgnoreCase(sub.getPlan().getId(), featureCode.trim());

        if (mappingOpt.isEmpty() || !mappingOpt.get().isEnabled()) {
            return UserEntitlementCheckResponseDTO.builder()
                    .userId(userId)
                    .featureCode(featureCode)
                    .hasEntitlement(false)
                    .planCode(sub.getPlan().getCode())
                    .expiresAt(sub.getEndDate())
                    .build();
        }

        PlanFeatureMappingEntity mapping = mappingOpt.get();
        return UserEntitlementCheckResponseDTO.builder()
                .userId(userId)
                .featureCode(featureCode)
                .hasEntitlement(true)
                .paramValue(mapping.getParamValue())
                .displayLabel(mapping.getDisplayLabel())
                .planCode(sub.getPlan().getCode())
                .expiresAt(sub.getEndDate())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserAllEntitlementsResponseDTO getAllUserEntitlements(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        Optional<UserSubscriptionEntity> activeSub = userSubscriptionRepository
                .findFirstByUserIdAndStatusAndEndDateAfterOrderByEndDateDesc(userId, "ACTIVE", now);

        if (activeSub.isEmpty()) {
            return UserAllEntitlementsResponseDTO.builder()
                    .userId(userId)
                    .hasActiveSubscription(false)
                    .entitlements(Collections.emptyList())
                    .build();
        }

        UserSubscriptionEntity sub = activeSub.get();
        List<PlanFeatureMappingEntity> mappings = planFeatureMappingRepository
                .findByPlanIdAndIsEnabledTrue(sub.getPlan().getId());

        List<PlanFeatureItemDTO> items = mappings.stream()
                .map(this::mapToFeatureItemDTO)
                .collect(Collectors.toList());

        return UserAllEntitlementsResponseDTO.builder()
                .userId(userId)
                .hasActiveSubscription(true)
                .planCode(sub.getPlan().getCode())
                .planName(sub.getPlan().getName())
                .expiresAt(sub.getEndDate())
                .entitlements(items)
                .build();
    }

    // ==========================================
    // 3. ADMIN MANAGEMENT ENDPOINTS
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponseDTO> getAllPlansForAdmin() {
        return subscriptionPlanRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::mapToPlanResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionPlanResponseDTO getPlanByIdForAdmin(Long planId) {
        SubscriptionPlanEntity plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy gói cước với ID: " + planId));
        return mapToPlanResponseDTO(plan);
    }

    @Override
    @Transactional
    public SubscriptionPlanResponseDTO updatePlanInfo(Long planId, UpdatePlanInfoRequestDTO request) {
        SubscriptionPlanEntity plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy gói cước với ID: " + planId));

        if (request.getName() != null && !request.getName().isBlank()) {
            plan.setName(request.getName());
        }
        if (request.getPriceVnd() != null) {
            plan.setPriceVnd(request.getPriceVnd());
        }
        if (request.getDurationDays() != null) {
            plan.setDurationDays(request.getDurationDays());
        }
        if (request.getDescription() != null) {
            plan.setDescription(request.getDescription());
        }
        if (request.getBadge() != null) {
            plan.setBadge(request.getBadge());
        }
        if (request.getIsActive() != null) {
            plan.setActive(request.getIsActive());
        }
        if (request.getDisplayOrder() != null) {
            plan.setDisplayOrder(request.getDisplayOrder());
        }
        plan.setUpdatedAt(LocalDateTime.now());

        subscriptionPlanRepository.save(plan);
        log.info("Admin cập nhật thông tin gói cước ID {}: name={}, price={}", planId, plan.getName(), plan.getPriceVnd());
        return mapToPlanResponseDTO(plan);
    }

    @Override
    @Transactional
    public SubscriptionPlanResponseDTO updatePlanFeatures(Long planId, UpdatePlanFeatureConfigRequestDTO request) {
        SubscriptionPlanEntity plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy gói cước với ID: " + planId));

        if (request == null || request.getFeatureConfigs() == null) {
            return mapToPlanResponseDTO(plan);
        }

        LocalDateTime now = LocalDateTime.now();
        for (UpdatePlanFeatureConfigRequestDTO.FeatureConfigItem item : request.getFeatureConfigs()) {
            if (item.getFeatureCode() == null || item.getFeatureCode().isBlank()) {
                continue;
            }

            SubscriptionFeatureEntity feature = subscriptionFeatureRepository
                    .findByFeatureCodeIgnoreCase(item.getFeatureCode().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tính năng: " + item.getFeatureCode()));

            Optional<PlanFeatureMappingEntity> existingMappingOpt = planFeatureMappingRepository
                    .findByPlanIdAndFeatureId(plan.getId(), feature.getId());

            PlanFeatureMappingEntity mapping;
            if (existingMappingOpt.isPresent()) {
                mapping = existingMappingOpt.get();
                mapping.setEnabled(item.isEnabled());
                if (item.getParamValue() != null) {
                    mapping.setParamValue(item.getParamValue());
                }
                if (item.getDisplayLabel() != null) {
                    mapping.setDisplayLabel(item.getDisplayLabel());
                }
                mapping.setUpdatedAt(now);
            } else {
                mapping = PlanFeatureMappingEntity.builder()
                        .plan(plan)
                        .feature(feature)
                        .isEnabled(item.isEnabled())
                        .paramValue(item.getParamValue() != null ? item.getParamValue() : feature.getDefaultValue())
                        .displayLabel(item.getDisplayLabel() != null ? item.getDisplayLabel() : feature.getName())
                        .updatedAt(now)
                        .build();
            }
            planFeatureMappingRepository.save(mapping);
            log.info("Admin cập nhật tính năng '{}' cho gói '{}': enabled={}, paramValue={}",
                    feature.getFeatureCode(), plan.getCode(), item.isEnabled(), mapping.getParamValue());
        }

        return mapToPlanResponseDTO(plan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionFeatureResponseDTO> getAllFeaturesForAdmin() {
        return subscriptionFeatureRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::mapToFeatureResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SubscriptionFeatureResponseDTO createFeature(CreateFeatureRequestDTO request) {
        if (subscriptionFeatureRepository.existsByFeatureCodeIgnoreCase(request.getFeatureCode())) {
            throw new IllegalArgumentException("Mã tính năng '" + request.getFeatureCode() + "' đã tồn tại trong hệ thống");
        }

        SubscriptionFeatureEntity feature = SubscriptionFeatureEntity.builder()
                .featureCode(request.getFeatureCode().trim().toUpperCase())
                .name(request.getName())
                .description(request.getDescription())
                .defaultValue(request.getDefaultValue())
                .unit(request.getUnit())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 99)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        subscriptionFeatureRepository.save(feature);
        log.info("Admin tạo tính năng mới: code={}, name={}", feature.getFeatureCode(), feature.getName());
        return mapToFeatureResponseDTO(feature);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSubscriptionDetailResponseDTO> getAllSubscribersForAdmin() {
        LocalDateTime now = LocalDateTime.now();
        List<UserSubscriptionEntity> allSubs = userSubscriptionRepository.findAllByOrderByCreatedAtDesc();

        return allSubs.stream()
                .map(sub -> {
                    long daysRemaining = Math.max(0, Duration.between(now, sub.getEndDate()).toDays());
                    return UserSubscriptionDetailResponseDTO.builder()
                            .userId(sub.getUserId())
                            .hasActiveSubscription(sub.isActive())
                            .planCode(sub.getPlan().getCode())
                            .planName(sub.getPlan().getName())
                            .badge(sub.getPlan().getBadge())
                            .startDate(sub.getStartDate())
                            .endDate(sub.getEndDate())
                            .daysRemaining(daysRemaining)
                            .activeFeatures(Collections.emptyList())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ==========================================
    // HELPER MAPPERS
    // ==========================================

    private SubscriptionPlanResponseDTO mapToPlanResponseDTO(SubscriptionPlanEntity plan) {
        List<PlanFeatureMappingEntity> mappings = planFeatureMappingRepository.findByPlanId(plan.getId());
        List<PlanFeatureItemDTO> featureItems = mappings.stream()
                .map(this::mapToFeatureItemDTO)
                .collect(Collectors.toList());

        return SubscriptionPlanResponseDTO.builder()
                .id(plan.getId())
                .code(plan.getCode())
                .name(plan.getName())
                .priceVnd(plan.getPriceVnd())
                .durationDays(plan.getDurationDays())
                .description(plan.getDescription())
                .badge(plan.getBadge())
                .isActive(plan.isActive())
                .displayOrder(plan.getDisplayOrder())
                .features(featureItems)
                .build();
    }

    private PlanFeatureItemDTO mapToFeatureItemDTO(PlanFeatureMappingEntity mapping) {
        SubscriptionFeatureEntity feature = mapping.getFeature();
        return PlanFeatureItemDTO.builder()
                .featureId(feature.getId())
                .featureCode(feature.getFeatureCode())
                .name(feature.getName())
                .description(feature.getDescription())
                .isEnabled(mapping.isEnabled())
                .paramValue(mapping.getParamValue())
                .displayLabel(mapping.getDisplayLabel())
                .unit(feature.getUnit())
                .build();
    }

    private SubscriptionFeatureResponseDTO mapToFeatureResponseDTO(SubscriptionFeatureEntity f) {
        return SubscriptionFeatureResponseDTO.builder()
                .id(f.getId())
                .featureCode(f.getFeatureCode())
                .name(f.getName())
                .description(f.getDescription())
                .defaultValue(f.getDefaultValue())
                .unit(f.getUnit())
                .displayOrder(f.getDisplayOrder())
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .build();
    }

    private long parseLongOrDefault(String str, long defaultVal) {
        if (str == null || str.isBlank()) return defaultVal;
        try {
            return Long.parseLong(str.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return defaultVal;
        }
    }
}
