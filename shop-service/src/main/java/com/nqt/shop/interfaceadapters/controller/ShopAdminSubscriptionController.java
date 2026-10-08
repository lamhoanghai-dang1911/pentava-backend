package com.nqt.shop.interfaceadapters.controller;

import com.nqt.shop.usecase.boundary.SubscriptionInputPort;
import com.nqt.shop.usecase.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Shop - Admin Subscription (Quản trị Gói cước & Quyền lợi)", description = "APIs dành cho Quản trị viên để cấu hình gói cước, bật/tắt hoặc hoán đổi tính năng giữa các gói")
@RestController
@RequestMapping("/api/shop/admin/subscription")
@RequiredArgsConstructor
public class ShopAdminSubscriptionController {

    private final SubscriptionInputPort subscriptionInputPort;

    @Operation(summary = "Lấy danh sách tất cả các gói cước trong hệ thống kèm theo cấu hình tính năng chi tiết")
    @GetMapping("/plans")
    public ResponseEntity<List<SubscriptionPlanResponseDTO>> getAllPlans() {
        return ResponseEntity.ok(subscriptionInputPort.getAllPlansForAdmin());
    }

    @Operation(summary = "Xem chi tiết một gói cước theo ID")
    @GetMapping("/plans/{planId}")
    public ResponseEntity<SubscriptionPlanResponseDTO> getPlanById(@PathVariable("planId") Long planId) {
        return ResponseEntity.ok(subscriptionInputPort.getPlanByIdForAdmin(planId));
    }

    @Operation(summary = "Cập nhật thông tin cơ bản của gói cước (Tên, giá tiền, thời hạn, huy hiệu, trạng thái bật/tắt bán)")
    @PutMapping("/plans/{planId}")
    public ResponseEntity<SubscriptionPlanResponseDTO> updatePlanInfo(
            @PathVariable("planId") Long planId,
            @Valid @RequestBody UpdatePlanInfoRequestDTO request) {
        return ResponseEntity.ok(subscriptionInputPort.updatePlanInfo(planId, request));
    }

    @Operation(summary = "Cập nhật cấu hình tính năng của gói cước (Bật/tắt tính năng, đổi tham số hạn mức, nhãn hiển thị)")
    @PutMapping("/plans/{planId}/features")
    public ResponseEntity<SubscriptionPlanResponseDTO> updatePlanFeatures(
            @PathVariable("planId") Long planId,
            @Valid @RequestBody UpdatePlanFeatureConfigRequestDTO request) {
        return ResponseEntity.ok(subscriptionInputPort.updatePlanFeatures(planId, request));
    }

    @Operation(summary = "Lấy danh mục tất cả tính năng có thể cấu hình trong hệ thống")
    @GetMapping("/features")
    public ResponseEntity<List<SubscriptionFeatureResponseDTO>> getAllFeatures() {
        return ResponseEntity.ok(subscriptionInputPort.getAllFeaturesForAdmin());
    }

    @Operation(summary = "Tạo thêm một tính năng mới vào danh mục hệ thống")
    @PostMapping("/features")
    public ResponseEntity<SubscriptionFeatureResponseDTO> createFeature(
            @Valid @RequestBody CreateFeatureRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionInputPort.createFeature(request));
    }

    @Operation(summary = "Lấy danh sách tất cả người dùng đã từng hoặc đang đăng ký gói cước")
    @GetMapping("/subscribers")
    public ResponseEntity<List<UserSubscriptionDetailResponseDTO>> getAllSubscribers() {
        return ResponseEntity.ok(subscriptionInputPort.getAllSubscribersForAdmin());
    }
}
