package com.nqt.skin.infrastructure.client;

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

    public boolean hasUnlockAllSkins(Long userId) {
        if (userId == null) return false;
        try {
            EntitlementCheckResponse response = restClient.get()
                    .uri("/api/shop/subscription/entitlement/check?userId={userId}&featureCode=UNLOCK_ALL_SKINS", userId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(EntitlementCheckResponse.class);

            return response != null && response.isHasEntitlement();
        } catch (Exception e) {
            log.warn("Không thể kiểm tra quyền UNLOCK_ALL_SKINS từ shop-service cho user {}: {}", userId, e.getMessage());
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
