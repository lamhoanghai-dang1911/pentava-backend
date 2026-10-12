package com.nqt.cinema.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class ShopSubscriptionClient {

    private final RestClient restClient;
    private static final long DEFAULT_FREE_QUOTA_BYTES = 500L * 1024 * 1024; // 500 MB cho tài khoản miễn phí

    public ShopSubscriptionClient(@Value("${shop-service.url:http://localhost:8087}") String shopServiceUrl) {
        log.info("Khởi tạo ShopSubscriptionClient trong cinema-service kết nối tới shop-service: {}", shopServiceUrl);
        this.restClient = RestClient.builder()
                .baseUrl(shopServiceUrl)
                .build();
    }

    public long getStorageQuotaBytes(Long userId) {
        if (userId == null) return DEFAULT_FREE_QUOTA_BYTES;
        try {
            EntitlementCheckResponse response = restClient.get()
                    .uri("/api/shop/subscription/entitlement/check?userId={userId}&featureCode=STORAGE_EXPANSION", userId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(EntitlementCheckResponse.class);

            if (response != null && response.isHasEntitlement() && response.getParamValue() != null) {
                return parseQuotaToBytes(response.getParamValue());
            }
        } catch (Exception e) {
            log.warn("Không thể lấy hạn mức dung lượng từ shop-service cho user {}: {}", userId, e.getMessage());
        }
        return DEFAULT_FREE_QUOTA_BYTES;
    }

    public boolean hasMultiAngleTemplates(Long userId) {
        if (userId == null) return false;
        try {
            EntitlementCheckResponse response = restClient.get()
                    .uri("/api/shop/subscription/entitlement/check?userId={userId}&featureCode=MULTI_ANGLE_TEMPLATES", userId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(EntitlementCheckResponse.class);

            return response != null && response.isHasEntitlement();
        } catch (Exception e) {
            log.warn("Không thể kiểm tra quyền MULTI_ANGLE_TEMPLATES từ shop-service cho user {}: {}", userId, e.getMessage());
            return false;
        }
    }

    private long parseQuotaToBytes(String paramValue) {
        try {
            String clean = paramValue.trim().toUpperCase();
            if (clean.endsWith("GB")) {
                long gb = Long.parseLong(clean.replace("GB", "").trim());
                return gb * 1024 * 1024 * 1024L;
            } else if (clean.endsWith("MB")) {
                long mb = Long.parseLong(clean.replace("MB", "").trim());
                return mb * 1024 * 1024L;
            }
        } catch (Exception e) {
            log.warn("Lỗi khi parse dung lượng '{}': {}", paramValue, e.getMessage());
        }
        return DEFAULT_FREE_QUOTA_BYTES;
    }

    public record UserPlanDetails(
            boolean hasActiveSubscription,
            String planCode,
            String planName,
            long storageQuotaBytes
    ) {}

    public UserPlanDetails getUserSubscriptionDetails(Long userId) {
        if (userId == null) {
            return new UserPlanDetails(false, "FREE", "Gói Miễn phí", DEFAULT_FREE_QUOTA_BYTES);
        }

        try {
            UserAllEntitlementsResponse response = restClient.get()
                    .uri("/api/shop/subscription/entitlements/{userId}", userId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(UserAllEntitlementsResponse.class);

            if (response != null && response.isHasActiveSubscription()) {
                long quota = DEFAULT_FREE_QUOTA_BYTES;
                if (response.getEntitlements() != null) {
                    for (PlanFeatureItem item : response.getEntitlements()) {
                        if ("STORAGE_EXPANSION".equalsIgnoreCase(item.getFeatureCode()) && item.isEnabled() && item.getParamValue() != null) {
                            quota = parseQuotaToBytes(item.getParamValue());
                            break;
                        }
                    }
                }
                return new UserPlanDetails(
                        true,
                        response.getPlanCode() != null ? response.getPlanCode() : "ACTIVE",
                        response.getPlanName() != null ? response.getPlanName() : "Gói thành viên",
                        quota
                );
            }
        } catch (Exception e) {
            log.warn("Không thể lấy thông tin gói cước từ shop-service cho user {}: {}", userId, e.getMessage());
        }

        return new UserPlanDetails(false, "FREE", "Gói Miễn phí", DEFAULT_FREE_QUOTA_BYTES);
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EntitlementCheckResponse {
        private Long userId;
        private String featureCode;
        private boolean hasEntitlement;
        private String paramValue;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserAllEntitlementsResponse {
        private Long userId;
        private boolean hasActiveSubscription;
        private String planCode;
        private String planName;
        private java.util.List<PlanFeatureItem> entitlements;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlanFeatureItem {
        private String featureCode;
        private String name;
        private String paramValue;
        private boolean enabled;
    }
}
