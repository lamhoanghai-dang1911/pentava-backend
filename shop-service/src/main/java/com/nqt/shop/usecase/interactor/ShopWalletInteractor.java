package com.nqt.shop.usecase.interactor;

import com.nqt.shop.domain.model.TopupStatus;
import com.nqt.shop.infrastructure.persistence.entity.TopupTransactionEntity;
import com.nqt.shop.infrastructure.persistence.entity.UserWalletEntity;
import com.nqt.shop.infrastructure.persistence.repository.TopupTransactionRepository;
import com.nqt.shop.infrastructure.persistence.repository.UserWalletRepository;
import com.nqt.shop.usecase.boundary.ShopWalletInputPort;
import com.nqt.shop.usecase.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopWalletInteractor implements ShopWalletInputPort {

    private final UserWalletRepository userWalletRepository;
    private final TopupTransactionRepository topupTransactionRepository;

    @Value("${sepay.api-key:your_sepay_api_key_here}")
    private String sepayApiKey;

    @Value("${sepay.bank-code:MBBank}")
    private String sepayBankCode;

    @Value("${sepay.account-number:0123456789}")
    private String sepayAccountNumber;

    @Value("${sepay.account-name:PENTAVA}")
    private String sepayAccountName;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Pattern CODE_PATTERN = Pattern.compile("PENTAVA\\d+", Pattern.CASE_INSENSITIVE);

    @Override
    @Transactional
    public WalletResponseDTO getWallet(Long userId) {
        UserWalletEntity wallet = userWalletRepository.findByUserId(userId)
                .orElseGet(() -> userWalletRepository.save(UserWalletEntity.builder()
                        .userId(userId)
                        .rubyBalance(0)
                        .updatedAt(LocalDateTime.now())
                        .build()));

        return WalletResponseDTO.builder()
                .userId(wallet.getUserId())
                .rubyBalance(wallet.getRubyBalance())
                .build();
    }

    @Override
    @Transactional
    public TopupInitResponseDTO initTopup(Long userId, Long amountVnd) {
        if (amountVnd == null || amountVnd < 10000) {
            throw new IllegalArgumentException("Số tiền nạp tối thiểu là 10.000 VNĐ");
        }

        // Tỷ lệ quy đổi: 1.000 VNĐ = 1 Ruby
        long rubyAmount = amountVnd / 1000;

        // Sinh mã giao dịch: ví dụ PENTAVA849201
        String txCode = "PENTAVA" + (100000 + RANDOM.nextInt(900000));

        TopupTransactionEntity tx = TopupTransactionEntity.builder()
                .userId(userId)
                .transactionCode(txCode)
                .amountVnd(amountVnd)
                .rubyAmount(rubyAmount)
                .status(TopupStatus.PENDING)
                .paymentMethod("SEPAY")
                .bankCode(sepayBankCode)
                .accountNumber(sepayAccountNumber)
                .accountName(sepayAccountName)
                .createdAt(LocalDateTime.now())
                .build();

        topupTransactionRepository.save(tx);

        // Sinh URL mã QR SePay
        String qrUrl = String.format(
                "https://qr.sepay.vn/img?acc=%s&bank=%s&amount=%d&des=%s&template=compact",
                URLEncoder.encode(sepayAccountNumber, StandardCharsets.UTF_8),
                URLEncoder.encode(sepayBankCode, StandardCharsets.UTF_8),
                amountVnd,
                URLEncoder.encode(txCode, StandardCharsets.UTF_8)
        );

        log.info("Initialized topup for user {}: txCode={}, amountVnd={}, rubyAmount={}",
                userId, txCode, amountVnd, rubyAmount);

        return TopupInitResponseDTO.builder()
                .transactionCode(txCode)
                .amountVnd(amountVnd)
                .rubyAmount(rubyAmount)
                .bankCode(sepayBankCode)
                .accountNumber(sepayAccountNumber)
                .accountName(sepayAccountName)
                .transferContent(txCode)
                .qrCodeUrl(qrUrl)
                .status(TopupStatus.PENDING)
                .build();
    }

    @Override
    @Transactional
    public void handleSepayWebhook(SepayWebhookPayloadDTO payload, String authHeader) {
        log.info("Received SePay webhook: {}", payload);

        // Kiểm tra API Key từ SePay header nếu đã được cấu hình khác giá trị placeholder
        if (sepayApiKey != null && !sepayApiKey.isBlank() && !"your_sepay_api_key_here".equals(sepayApiKey)) {
            String expected = "Apikey " + sepayApiKey;
            boolean isSepayApiKey = authHeader != null && (
                    authHeader.trim().equalsIgnoreCase(expected)
                    || authHeader.trim().equalsIgnoreCase(sepayApiKey)
                    || authHeader.trim().contains(sepayApiKey)
            );
            // Cho phép cả Bearer token khi dev/tester test trực tiếp trên Swagger UI
            boolean isBearerToken = authHeader != null && authHeader.trim().startsWith("Bearer ");

            if (!isSepayApiKey && !isBearerToken) {
                log.warn("Unauthorized SePay webhook call. Expected header: {} hoặc Bearer token, Actual: {}", expected, authHeader);
                throw new IllegalStateException("Unauthorized SePay Webhook");
            }
        }

        if (payload == null || !"in".equalsIgnoreCase(payload.getTransferType())) {
            log.info("Bỏ qua giao dịch không phải chiều tiền vào (in): {}", payload);
            return;
        }

        // Tìm mã giao dịch trong nội dung chuyển khoản
        String foundCode = extractTransactionCode(payload.getContent());
        if (foundCode == null && payload.getCode() != null) {
            foundCode = extractTransactionCode(payload.getCode());
        }

        if (foundCode == null) {
            log.warn("Không tìm thấy mã giao dịch PENTAVA trong nội dung: {}", payload.getContent());
            return;
        }

        String finalTxCode = foundCode.toUpperCase();
        TopupTransactionEntity tx = topupTransactionRepository.findByTransactionCode(finalTxCode)
                .orElse(null);

        if (tx == null) {
            log.warn("Không tìm thấy giao dịch với mã: {}", finalTxCode);
            return;
        }

        if (tx.getStatus() == TopupStatus.SUCCESS) {
            log.info("Giao dịch {} đã được xử lý thành công trước đó", finalTxCode);
            return;
        }

        long transferred = payload.getTransferAmount() != null ? payload.getTransferAmount() : 0L;
        if (transferred < tx.getAmountVnd()) {
            log.warn("Số tiền chuyển ({} VNĐ) nhỏ hơn số tiền yêu cầu ({} VNĐ) cho mã {}",
                    transferred, tx.getAmountVnd(), finalTxCode);
            return;
        }

        // Thành công: Cập nhật trạng thái và cộng Ruby vào ví
        tx.setStatus(TopupStatus.SUCCESS);
        tx.setCompletedAt(LocalDateTime.now());
        tx.setSepayReferenceCode(payload.getReferenceCode());
        topupTransactionRepository.save(tx);

        creditRubyToUser(tx.getUserId(), tx.getRubyAmount());
        log.info("Processed SePay topup successfully: txCode={}, userId={}, rubyAdded={}",
                finalTxCode, tx.getUserId(), tx.getRubyAmount());
    }

    @Override
    @Transactional
    public void mockConfirmTopup(String transactionCode) {
        TopupTransactionEntity tx = topupTransactionRepository.findByTransactionCode(transactionCode.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch: " + transactionCode));

        if (tx.getStatus() == TopupStatus.SUCCESS) {
            log.info("Giao dịch {} đã hoàn thành trước đó", transactionCode);
            return;
        }

        tx.setStatus(TopupStatus.SUCCESS);
        tx.setCompletedAt(LocalDateTime.now());
        topupTransactionRepository.save(tx);

        creditRubyToUser(tx.getUserId(), tx.getRubyAmount());
        log.info("Mock confirmed topup: txCode={}, userId={}, rubyAdded={}",
                transactionCode, tx.getUserId(), tx.getRubyAmount());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopupHistoryResponseDTO> getTopupHistory(Long userId) {
        return topupTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(tx -> TopupHistoryResponseDTO.builder()
                        .id(tx.getId())
                        .transactionCode(tx.getTransactionCode())
                        .amountVnd(tx.getAmountVnd())
                        .rubyAmount(tx.getRubyAmount())
                        .status(tx.getStatus())
                        .paymentMethod(tx.getPaymentMethod())
                        .createdAt(tx.getCreatedAt())
                        .completedAt(tx.getCompletedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private void creditRubyToUser(Long userId, long rubyAmount) {
        UserWalletEntity wallet = userWalletRepository.findByUserId(userId)
                .orElseGet(() -> UserWalletEntity.builder()
                        .userId(userId)
                        .rubyBalance(0)
                        .updatedAt(LocalDateTime.now())
                        .build());

        wallet.setRubyBalance(wallet.getRubyBalance() + rubyAmount);
        wallet.setUpdatedAt(LocalDateTime.now());
        userWalletRepository.save(wallet);
    }

    private String extractTransactionCode(String content) {
        if (content == null) {
            return null;
        }
        Matcher matcher = CODE_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
}
