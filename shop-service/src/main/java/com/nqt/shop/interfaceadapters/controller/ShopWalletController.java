package com.nqt.shop.interfaceadapters.controller;

import com.nqt.shop.infrastructure.security.CurrentUserProvider;
import com.nqt.shop.usecase.boundary.ShopWalletInputPort;
import com.nqt.shop.usecase.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Shop - Wallet & Topup", description = "Quản lý ví Ruby, nạp tiền quy đổi Ruby qua SePay / VietQR")
@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopWalletController {

    private final ShopWalletInputPort shopWalletInputPort;
    private final CurrentUserProvider currentUserProvider;

    @Operation(summary = "Xem bảng giá nạp Ruby (tỷ lệ nạp lẻ 10 Ruby = 20.000 VNĐ và các gói Small, Medium, Large Pack)")
    @GetMapping("/topup/packages")
    public ResponseEntity<RubyTopupPricingResponseDTO> getTopupPackages() {
        return ResponseEntity.ok(shopWalletInputPort.getTopupPricing());
    }

    @Operation(summary = "Xem số dư Ruby trong ví của người dùng hiện tại")
    @GetMapping("/wallet/me")
    public ResponseEntity<WalletResponseDTO> getMyWallet() {
        Long userId = currentUserProvider.resolveCurrentUserId();
        return ResponseEntity.ok(shopWalletInputPort.getWallet(userId));
    }

    @Operation(summary = "Khởi tạo yêu cầu nạp tiền quy đổi Ruby (Chọn gói nạp hoặc nạp lẻ để tạo mã QR SePay / VietQR)")
    @PostMapping("/topup/init")
    public ResponseEntity<TopupInitResponseDTO> initTopup(@Valid @RequestBody TopupInitRequestDTO request) {
        Long userId = currentUserProvider.resolveCurrentUserId();
        return ResponseEntity.ok(shopWalletInputPort.initTopup(userId, request));
    }

    @Operation(summary = "Xem lịch sử các giao dịch nạp tiền của người dùng")
    @GetMapping("/topup/history")
    public ResponseEntity<List<TopupHistoryResponseDTO>> getTopupHistory() {
        Long userId = currentUserProvider.resolveCurrentUserId();
        return ResponseEntity.ok(shopWalletInputPort.getTopupHistory(userId));
    }

    @Operation(summary = "Endpoint mô phỏng nạp tiền thành công (Dành riêng cho Local / Dev testing mà không cần chuyển khoản thật)")
    @PostMapping("/topup/mock-confirm/{transactionCode}")
    public ResponseEntity<Map<String, Object>> mockConfirm(
            @io.swagger.v3.oas.annotations.Parameter(description = "Mã giao dịch nạp tiền (ví dụ: PENTAVA237116)", required = true, example = "PENTAVA237116")
            @PathVariable("transactionCode") String transactionCode) {
        shopWalletInputPort.mockConfirmTopup(transactionCode);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Mô phỏng nạp tiền thành công cho mã giao dịch: " + transactionCode
        ));
    }
}
