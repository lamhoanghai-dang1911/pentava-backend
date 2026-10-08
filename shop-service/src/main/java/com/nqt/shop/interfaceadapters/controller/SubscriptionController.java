package com.nqt.shop.interfaceadapters.controller;

import com.nqt.shop.infrastructure.security.CurrentUserProvider;
import com.nqt.shop.usecase.boundary.SubscriptionInputPort;
import com.nqt.shop.usecase.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Shop - Subscription Plans (Gói cước)", description = "Quản lý mua và sử dụng các gói dịch vụ nâng cao (BASIC, GOLD, PREMIUM) qua SePay VietQR")
@RestController
@RequestMapping("/api/shop/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionInputPort subscriptionInputPort;
    private final CurrentUserProvider currentUserProvider;

    @Operation(summary = "Xem danh sách các gói cước đang mở bán kèm các tính năng tương ứng (Công khai)")
    @GetMapping("/plans")
    public ResponseEntity<List<SubscriptionPlanResponseDTO>> getActivePlans() {
        return ResponseEntity.ok(subscriptionInputPort.getActivePlans());
    }

    @Operation(summary = "Xem thông tin gói cước và quyền lợi hiện tại của người dùng đang đăng nhập")
    @GetMapping("/my-subscription")
    public ResponseEntity<UserSubscriptionDetailResponseDTO> getMySubscription() {
        Long userId = currentUserProvider.resolveCurrentUserId();
        return ResponseEntity.ok(subscriptionInputPort.getMySubscription(userId));
    }

    @Operation(summary = "Khởi tạo yêu cầu mua gói cước (Tạo mã giao dịch và mã VietQR SePay để thanh toán)")
    @PostMapping("/init")
    public ResponseEntity<SubscriptionPurchaseInitResponseDTO> initPurchase(
            @Valid @RequestBody SubscriptionPurchaseInitRequestDTO request) {
        Long userId = currentUserProvider.resolveCurrentUserId();
        return ResponseEntity.ok(subscriptionInputPort.initPurchase(userId, request));
    }

    @Operation(summary = "Giả lập thanh toán thành công cho đơn mua gói cước (Dành cho Dev / Test không cần chuyển khoản thật)")
    @PostMapping("/mock-confirm/{transactionCode}")
    public ResponseEntity<Map<String, Object>> mockConfirmPurchase(
            @PathVariable("transactionCode") String transactionCode) {
        subscriptionInputPort.mockConfirmPurchase(transactionCode);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Mô phỏng thanh toán thành công và kích hoạt gói cước cho mã giao dịch: " + transactionCode
        ));
    }

    @Operation(summary = "Kiểm tra xem một người dùng có sở hữu tính năng nhất định hay không (Dành cho client / internal services)")
    @GetMapping("/entitlement/check")
    public ResponseEntity<UserEntitlementCheckResponseDTO> checkEntitlement(
            @RequestParam("userId") Long userId,
            @RequestParam("featureCode") String featureCode) {
        return ResponseEntity.ok(subscriptionInputPort.checkUserEntitlement(userId, featureCode));
    }

    @Operation(summary = "Lấy tất cả các quyền lợi còn hiệu lực của một người dùng")
    @GetMapping("/entitlements/{userId}")
    public ResponseEntity<UserAllEntitlementsResponseDTO> getAllEntitlements(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(subscriptionInputPort.getAllUserEntitlements(userId));
    }
}
