package com.nqt.manage.interfaceadapters.controller;

import com.nqt.manage.infrastructure.gateway.ShopServiceSubscriptionGateway;
import com.nqt.manage.usecase.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Manage - Subscription (Admin)", description = "Quản lý Gói cước & Tùy biến tính năng linh hoạt cho Quản trị viên (Admin Web)")
@RestController
@RequestMapping("/api/manage/subscription")
@RequiredArgsConstructor
public class AdminSubscriptionController {

    private final ShopServiceSubscriptionGateway subscriptionGateway;

    @Operation(summary = "Lấy danh sách tất cả các gói cước (BASIC, GOLD, PREMIUM) kèm cấu hình tính năng chi tiết")
    @GetMapping("/plans")
    public ResponseEntity<List<SubscriptionPlanResponseDTO>> getAllPlans() {
        return ResponseEntity.ok(subscriptionGateway.getAllPlans());
    }

    @Operation(summary = "Xem thông tin chi tiết một gói cước theo Plan ID")
    @GetMapping("/plans/{planId}")
    public ResponseEntity<SubscriptionPlanResponseDTO> getPlanById(@PathVariable("planId") Long planId) {
        return ResponseEntity.ok(subscriptionGateway.getPlanById(planId));
    }

    @Operation(summary = "Cập nhật thông tin cơ bản gói cước (Tên, giá tiền VND, thời hạn ngày, mô tả, bật/tắt mở bán)")
    @PutMapping("/plans/{planId}")
    public ResponseEntity<SubscriptionPlanResponseDTO> updatePlanInfo(
            @PathVariable("planId") Long planId,
            @Valid @RequestBody UpdatePlanInfoRequestDTO request) {
        return ResponseEntity.ok(subscriptionGateway.updatePlanInfo(planId, request));
    }

    @Operation(summary = "Tùy biến tính năng trong gói: Bật/Tắt (tick/bỏ tick), chỉnh sửa hạn mức hoặc câu văn hiển thị của tính năng")
    @PutMapping("/plans/{planId}/features")
    public ResponseEntity<SubscriptionPlanResponseDTO> updatePlanFeatures(
            @PathVariable("planId") Long planId,
            @Valid @RequestBody UpdatePlanFeatureConfigRequestDTO request) {
        return ResponseEntity.ok(subscriptionGateway.updatePlanFeatures(planId, request));
    }

    @Operation(summary = "Lấy danh mục tất cả tính năng có thể phân bổ trong hệ thống Pentava")
    @GetMapping("/features")
    public ResponseEntity<List<SubscriptionFeatureResponseDTO>> getAllFeatures() {
        return ResponseEntity.ok(subscriptionGateway.getAllFeatures());
    }

    @Operation(summary = "Thêm tính năng mới vào danh mục hệ thống Pentava")
    @PostMapping("/features")
    public ResponseEntity<SubscriptionFeatureResponseDTO> createFeature(
            @Valid @RequestBody CreateFeatureRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionGateway.createFeature(request));
    }

    @Operation(summary = "Lấy danh sách người dùng đã đăng ký các gói cước")
    @GetMapping("/subscribers")
    public ResponseEntity<List<UserSubscriptionDetailResponseDTO>> getAllSubscribers() {
        return ResponseEntity.ok(subscriptionGateway.getAllSubscribers());
    }
}
