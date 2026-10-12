package com.nqt.onboarding.infrastructure.client;

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

    public ShopSubscriptionClient(@Value("${shop-service.url:http://localhost:8087}") String shopServiceUrl) {
        log.info("Khởi tạo ShopSubscriptionClient kết nối tới shop-service: {}", shopServiceUrl);
        this.restClient = RestClient.builder()
                .baseUrl(shopServiceUrl)
                .build();
    }

    public boolean hasStreakProtection(Long userId) {
        return checkEntitlement(userId, "STREAK_PROTECTION");
    }

    public boolean hasCustomRoadmap(Long userId) {
        return checkEntitlement(userId, "CUSTOM_ROADMAP");
    }

    public boolean checkEntitlement(Long userId, String featureCode) {
        if (userId == null) return false;
        try {
            EntitlementCheckResponse response = restClient.get()
                    .uri("/api/shop/subscription/entitlement/check?userId={userId}&featureCode={featureCode}", userId, featureCode)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(EntitlementCheckResponse.class);

            return response != null && response.isHasEntitlement();
        } catch (Exception e) {
            log.warn("Không thể kiểm tra quyền {} từ shop-service cho user {}: {}", featureCode, userId, e.getMessage());
            return false;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EntitlementCheckResponse {
        private Long userId;
        private String featureCode;
        private boolean hasEntitlement;
        private String paramValue;
    }
}
