package com.nqt.shop.interfaceadapters.controller;

import com.nqt.shop.usecase.boundary.ShopWalletInputPort;
import com.nqt.shop.usecase.dto.SepayWebhookPayloadDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@Tag(name = "Shop - SePay Webhook", description = "Webhook tiếp nhận thông báo biến động số dư chuyển khoản từ cổng thanh toán SePay")
@RestController
@RequestMapping("/api/shop/webhook")
@RequiredArgsConstructor
public class SepayWebhookController {

    private final ShopWalletInputPort shopWalletInputPort;

    @Operation(summary = "Webhook tự động nhận callback từ SePay khi có giao dịch chuyển khoản")
    @PostMapping("/sepay")
    public ResponseEntity<Map<String, Object>> handleSepayWebhook(
            @RequestBody SepayWebhookPayloadDTO payload,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("Incoming SePay Webhook request: payload={}", payload);
        shopWalletInputPort.handleSepayWebhook(payload, authHeader);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
