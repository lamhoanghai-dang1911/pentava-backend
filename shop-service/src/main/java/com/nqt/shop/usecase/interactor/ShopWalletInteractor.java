package com.nqt.shop.usecase.interactor;

import com.nqt.shop.domain.model.RubyPackage;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopWalletInteractor implements ShopWalletInputPort {

    public static final long BASE_VND_PER_RUBY = 2000L;
    public static final long MIN_RUBY = 10L;
    public static final long MIN_AMOUNT_VND = 20000L;

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
    @Transactional(readOnly = true)
    public RubyTopupPricingResponseDTO getTopupPricing() {
        List<RubyPackageDTO> packageDTOs = Arrays.stream(RubyPackage.values())
                .map(p -> RubyPackageDTO.builder()
                        .code(p.getCode())
                        .name(p.getName())
                        .rubyAmount(p.getRubyAmount())
                        .priceVnd(p.getPriceVnd())
                        .originalPriceVnd(p.getRubyAmount() * BASE_VND_PER_RUBY)
                        .discountVnd(p.getDiscountVnd())
                        .bonusRuby(p.getBonusRuby())
                        .description(p.getDescription())
                        .isPopular(p.isPopular())
                        .isBestValue(p.isBestValue())
                        .build())
                .toList();

        return RubyTopupPricingResponseDTO.builder()
                .baseRateVndPerRuby(BASE_VND_PER_RUBY)
                .minCustomTopupVnd(MIN_AMOUNT_VND)
                .minCustomTopupRuby(MIN_RUBY)
                .customTopupRule("10 ruby = 20.000 VNĐ (1 ruby = 2.000 VNĐ, tối thiểu 10 ruby)")
                .packages(packageDTOs)
                .build();
    }

    @Override
    @Transactional
    public TopupInitResponseDTO initTopup(Long userId, Long amountVnd) {
        return initTopup(userId, TopupInitRequestDTO.builder().amountVnd(amountVnd).build());
    }

    @Override
    @Transactional
    public TopupInitResponseDTO initTopup(Long userId, TopupInitRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Yêu cầu nạp tiền không hợp lệ");
        }

        String packageCode;
        String packageName;
        String description;
        long amountVnd;
        long rubyAmount;

        // 1. Trường hợp người dùng chọn gói nạp Ruby
        if (request.getPackageCode() != null && !request.getPackageCode().isBlank()) {
            RubyPackage pack = RubyPackage.fromCode(request.getPackageCode())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Gói nạp Ruby không hợp lệ: '" + request.getPackageCode() + "'. Các gói hợp lệ: SMALL, MEDIUM, LARGE"
                    ));
            packageCode = pack.getCode();
            packageName = pack.getName();
            amountVnd = pack.getPriceVnd();
            rubyAmount = pack.getRubyAmount();
            description = pack.getDescription();
        }
        // 2. Trường hợp nạp lẻ hoặc người dùng truyền số tiền VNĐ
        else if (request.getAmountVnd() != null && request.getAmountVnd() > 0) {
            Optional<RubyPackage> matchedPack = RubyPackage.fromAmountVnd(request.getAmountVnd());
            if (matchedPack.isPresent()) {
                RubyPackage pack = matchedPack.get();
                packageCode = pack.getCode();
                packageName = pack.getName();
                amountVnd = pack.getPriceVnd();
                rubyAmount = pack.getRubyAmount();
                description = pack.getDescription();
            } else {
                if (request.getAmountVnd() < MIN_AMOUNT_VND) {
                    throw new IllegalArgumentException("Số tiền nạp tối thiểu là 20.000 VNĐ (tương đương 10 Ruby)");
                }
                if (request.getAmountVnd() % BASE_VND_PER_RUBY != 0) {
                    throw new IllegalArgumentException("Số tiền nạp lẻ phải là bội số của 2.000 VNĐ (10 Ruby = 20.000 VNĐ)");
                }
                packageCode = "CUSTOM";
                packageName = "Nạp lẻ";
                amountVnd = request.getAmountVnd();
                rubyAmount = amountVnd / BASE_VND_PER_RUBY;
                description = String.format("Nạp lẻ %d Ruby = %,d VNĐ", rubyAmount, amountVnd);
            }
        }
        // 3. Trường hợp nạp lẻ truyền số Ruby mong muốn
        else if (request.getRubyAmount() != null && request.getRubyAmount() > 0) {
            Optional<RubyPackage> matchedPack = RubyPackage.fromRubyAmount(request.getRubyAmount());
            if (matchedPack.isPresent()) {
                RubyPackage pack = matchedPack.get();
                packageCode = pack.getCode();
                packageName = pack.getName();
                amountVnd = pack.getPriceVnd();
                rubyAmount = pack.getRubyAmount();
                description = pack.getDescription();
            } else {
                if (request.getRubyAmount() < MIN_RUBY) {
                    throw new IllegalArgumentException("Số Ruby nạp tối thiểu là 10 Ruby (tương đương 20.000 VNĐ)");
                }
                packageCode = "CUSTOM";
                packageName = "Nạp lẻ";
                rubyAmount = request.getRubyAmount();
                amountVnd = rubyAmount * BASE_VND_PER_RUBY;
                description = String.format("Nạp lẻ %d Ruby = %,d VNĐ", rubyAmount, amountVnd);
            }
        }
        // 4. Không có thông tin hợp lệ
        else {
            throw new IllegalArgumentException("Vui lòng chọn gói nạp Ruby (SMALL, MEDIUM, LARGE) hoặc nhập số tiền nạp lẻ (tối thiểu 20.000 VNĐ = 10 Ruby)");
        }

        // Sinh mã giao dịch: ví dụ PENTAVA849201
        String txCode = "PENTAVA" + (100000 + RANDOM.nextInt(900000));

        TopupTransactionEntity tx = TopupTransactionEntity.builder()
                .userId(userId)
                .transactionCode(txCode)
                .amountVnd(amountVnd)
                .rubyAmount(rubyAmount)
                .packageCode(packageCode)
                .packageName(packageName)
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

        log.info("Initialized topup for user {}: txCode={}, package={}, amountVnd={}, rubyAmount={}",
                userId, txCode, packageCode, amountVnd, rubyAmount);

        return TopupInitResponseDTO.builder()
                .transactionCode(txCode)
                .amountVnd(amountVnd)
                .rubyAmount(rubyAmount)
                .packageCode(packageCode)
                .packageName(packageName)
                .description(description)
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
                        .packageCode(tx.getPackageCode())
                        .packageName(tx.getPackageName())
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
