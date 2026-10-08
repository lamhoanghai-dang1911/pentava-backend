package com.nqt.manage.infrastructure.gateway;

import com.nqt.manage.usecase.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class ShopServiceSubscriptionGateway {

    private final RestClient restClient;

    public ShopServiceSubscriptionGateway(@Value("${shop-service.url:http://localhost:8087}") String shopServiceUrl) {
        log.info("Khởi tạo ShopServiceSubscriptionGateway kết nối tới shop-service: {}", shopServiceUrl);
        this.restClient = RestClient.builder()
                .baseUrl(shopServiceUrl)
                .defaultHeader("X-Internal-Caller", "manage-service")
                .build();
    }

    public List<SubscriptionPlanResponseDTO> getAllPlans() {
        try {
            return restClient.get()
                    .uri("/api/shop/admin/subscription/plans")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<SubscriptionPlanResponseDTO>>() {});
        } catch (Exception e) {
            log.error("Lỗi khi gọi shop-service lấy danh sách gói cước: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể kết nối đến Shop Service để lấy danh sách gói cước: " + e.getMessage());
        }
    }

    public SubscriptionPlanResponseDTO getPlanById(Long planId) {
        try {
            return restClient.get()
                    .uri("/api/shop/admin/subscription/plans/{planId}", planId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(SubscriptionPlanResponseDTO.class);
        } catch (Exception e) {
            log.error("Lỗi khi gọi shop-service lấy chi tiết gói cước {}: {}", planId, e.getMessage(), e);
            throw new RuntimeException("Không thể kết nối đến Shop Service: " + e.getMessage());
        }
    }

    public SubscriptionPlanResponseDTO updatePlanInfo(Long planId, UpdatePlanInfoRequestDTO request) {
        try {
            return restClient.put()
                    .uri("/api/shop/admin/subscription/plans/{planId}", planId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(SubscriptionPlanResponseDTO.class);
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật thông tin gói cước {}: {}", planId, e.getMessage(), e);
            throw new RuntimeException("Không thể cập nhật thông tin gói cước qua Shop Service: " + e.getMessage());
        }
    }

    public SubscriptionPlanResponseDTO updatePlanFeatures(Long planId, UpdatePlanFeatureConfigRequestDTO request) {
        try {
            return restClient.put()
                    .uri("/api/shop/admin/subscription/plans/{planId}/features", planId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(SubscriptionPlanResponseDTO.class);
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật tính năng cho gói cước {}: {}", planId, e.getMessage(), e);
            throw new RuntimeException("Không thể cấu hình tính năng gói cước qua Shop Service: " + e.getMessage());
        }
    }

    public List<SubscriptionFeatureResponseDTO> getAllFeatures() {
        try {
            return restClient.get()
                    .uri("/api/shop/admin/subscription/features")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<SubscriptionFeatureResponseDTO>>() {});
        } catch (Exception e) {
            log.error("Lỗi khi gọi shop-service lấy danh mục tính năng: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể kết nối đến Shop Service để lấy danh mục tính năng: " + e.getMessage());
        }
    }

    public SubscriptionFeatureResponseDTO createFeature(CreateFeatureRequestDTO request) {
        try {
            return restClient.post()
                    .uri("/api/shop/admin/subscription/features")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(SubscriptionFeatureResponseDTO.class);
        } catch (Exception e) {
            log.error("Lỗi khi tạo tính năng mới: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo tính năng mới qua Shop Service: " + e.getMessage());
        }
    }

    public List<UserSubscriptionDetailResponseDTO> getAllSubscribers() {
        try {
            return restClient.get()
                    .uri("/api/shop/admin/subscription/subscribers")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<UserSubscriptionDetailResponseDTO>>() {});
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách người dùng đăng ký gói: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
