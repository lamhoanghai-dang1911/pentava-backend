package com.nqt.shop.usecase.interactor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nqt.shop.domain.model.TopupStatus;
import com.nqt.shop.infrastructure.persistence.entity.CashflowRecordEntity;
import com.nqt.shop.infrastructure.persistence.entity.PurchaseTransactionEntity;
import com.nqt.shop.infrastructure.persistence.entity.ShopItemEntity;
import com.nqt.shop.infrastructure.persistence.entity.SubscriptionOrderEntity;
import com.nqt.shop.infrastructure.persistence.entity.TopupTransactionEntity;
import com.nqt.shop.infrastructure.persistence.repository.CashflowRecordRepository;
import com.nqt.shop.infrastructure.persistence.repository.PurchaseTransactionRepository;
import com.nqt.shop.infrastructure.persistence.repository.ShopItemRepository;
import com.nqt.shop.infrastructure.persistence.repository.SubscriptionOrderRepository;
import com.nqt.shop.infrastructure.persistence.repository.TopupTransactionRepository;
import com.nqt.shop.infrastructure.persistence.repository.UserWalletRepository;
import com.nqt.shop.infrastructure.realtime.CashflowRealtimeService;
import com.nqt.shop.usecase.boundary.AuthServiceGateway;
import com.nqt.shop.usecase.boundary.CashflowInputPort;
import com.nqt.shop.usecase.boundary.ShopWalletInputPort;
import com.nqt.shop.usecase.boundary.SubscriptionInputPort;
import com.nqt.shop.usecase.dto.CashflowItemDetailsDTO;
import com.nqt.shop.usecase.dto.CashflowRecordResponseDTO;
import com.nqt.shop.usecase.dto.CashflowSummaryResponseDTO;
import com.nqt.shop.usecase.dto.UserDetailDTO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CashflowInteractor implements CashflowInputPort {

    private final CashflowRecordRepository cashflowRecordRepository;
    private final CashflowRealtimeService cashflowRealtimeService;
    private final AuthServiceGateway authServiceGateway;
    private final UserWalletRepository userWalletRepository;
    private final TopupTransactionRepository topupTransactionRepository;
    private final SubscriptionOrderRepository subscriptionOrderRepository;
    private final PurchaseTransactionRepository purchaseTransactionRepository;
    private final ShopItemRepository shopItemRepository;
    private final ObjectMapper objectMapper;

    private final ShopWalletInputPort shopWalletInputPort;
    private final SubscriptionInputPort subscriptionInputPort;

    // Cache thông tin user trong bộ nhớ tạm thời để giảm tải gọi gRPC liên tục
    private final Map<Long, UserDetailDTO> userCache = new ConcurrentHashMap<>();

    public CashflowInteractor(
            CashflowRecordRepository cashflowRecordRepository,
            CashflowRealtimeService cashflowRealtimeService,
            AuthServiceGateway authServiceGateway,
            UserWalletRepository userWalletRepository,
            TopupTransactionRepository topupTransactionRepository,
            SubscriptionOrderRepository subscriptionOrderRepository,
            PurchaseTransactionRepository purchaseTransactionRepository,
            ShopItemRepository shopItemRepository,
            ObjectMapper objectMapper,
            @Lazy ShopWalletInputPort shopWalletInputPort,
            @Lazy SubscriptionInputPort subscriptionInputPort) {
        this.cashflowRecordRepository = cashflowRecordRepository;
        this.cashflowRealtimeService = cashflowRealtimeService;
        this.authServiceGateway = authServiceGateway;
        this.userWalletRepository = userWalletRepository;
        this.topupTransactionRepository = topupTransactionRepository;
        this.subscriptionOrderRepository = subscriptionOrderRepository;
        this.purchaseTransactionRepository = purchaseTransactionRepository;
        this.shopItemRepository = shopItemRepository;
        this.objectMapper = objectMapper;
        this.shopWalletInputPort = shopWalletInputPort;
        this.subscriptionInputPort = subscriptionInputPort;
    }

    @PostConstruct
    public void initBackfill() {
        try {
            long count = cashflowRecordRepository.count();
            if (count == 0) {
                log.info("Bảng cashflow_records đang trống. Tiến hành đồng bộ lịch sử giao dịch ban đầu...");
                syncHistoricalData();
            }
        } catch (Exception e) {
            log.warn("Không thể đồng bộ lịch sử ban đầu cho sổ quỹ dòng tiền: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public CashflowRecordResponseDTO recordTopupPending(TopupTransactionEntity tx) {
        UserDetailDTO user = getUserDetail(tx.getUserId());

        CashflowItemDetailsDTO details = CashflowItemDetailsDTO.builder()
                .itemCode(tx.getPackageCode())
                .itemName(tx.getPackageName())
                .bonusRuby(0L)
                .build();

        String desc = tx.getPackageName() != null
                ? String.format("Nạp %s (%d Ruby = %,d VNĐ) - Đang chờ quét QR", tx.getPackageName(), tx.getRubyAmount(), tx.getAmountVnd())
                : String.format("Nạp lẻ %d Ruby = %,d VNĐ - Đang chờ quét QR", tx.getRubyAmount(), tx.getAmountVnd());

        CashflowRecordEntity entity = cashflowRecordRepository.findByTransactionCode(tx.getTransactionCode())
                .orElseGet(() -> CashflowRecordEntity.builder()
                        .transactionCode(tx.getTransactionCode())
                        .type("TOPUP_RUBY")
                        .direction("IN")
                        .createdAt(tx.getCreatedAt() != null ? tx.getCreatedAt() : LocalDateTime.now())
                        .build());

        entity.setAmountVnd(tx.getAmountVnd());
        entity.setRubyAmount(tx.getRubyAmount());
        entity.setUserId(tx.getUserId());
        entity.setUserName(user != null ? user.getName() : "Người dùng #" + tx.getUserId());
        entity.setUserEmail(user != null ? user.getEmail() : null);
        entity.setStatus("PENDING");
        entity.setPaymentMethod("SEPAY");
        entity.setBankCode(tx.getBankCode());
        entity.setAccountNumber(tx.getAccountNumber());
        entity.setAccountName(tx.getAccountName());
        entity.setDescription(desc);
        entity.setItemDetailsJson(toJson(details));

        CashflowRecordEntity saved = cashflowRecordRepository.save(entity);
        CashflowRecordResponseDTO dto = mapToDTO(saved);

        // Bắn realtime cho admin biết có đơn nạp mới vừa khởi tạo
        cashflowRealtimeService.broadcastTransaction(dto);
        return dto;
    }

    @Override
    @Transactional
    public CashflowRecordResponseDTO recordTopupSuccess(TopupTransactionEntity tx) {
        UserDetailDTO user = getUserDetail(tx.getUserId());

        CashflowItemDetailsDTO details = CashflowItemDetailsDTO.builder()
                .itemCode(tx.getPackageCode())
                .itemName(tx.getPackageName())
                .build();

        String desc = tx.getPackageName() != null
                ? String.format("Nạp %s (%d Ruby = %,d VNĐ)", tx.getPackageName(), tx.getRubyAmount(), tx.getAmountVnd())
                : String.format("Nạp lẻ %d Ruby = %,d VNĐ", tx.getRubyAmount(), tx.getAmountVnd());

        CashflowRecordEntity entity = cashflowRecordRepository.findByTransactionCode(tx.getTransactionCode())
                .orElseGet(() -> CashflowRecordEntity.builder()
                        .transactionCode(tx.getTransactionCode())
                        .type("TOPUP_RUBY")
                        .direction("IN")
                        .createdAt(tx.getCreatedAt() != null ? tx.getCreatedAt() : LocalDateTime.now())
                        .build());

        entity.setAmountVnd(tx.getAmountVnd());
        entity.setRubyAmount(tx.getRubyAmount());
        entity.setUserId(tx.getUserId());
        if (user != null) {
            entity.setUserName(user.getName());
            entity.setUserEmail(user.getEmail());
        }
        entity.setStatus("SUCCESS");
        entity.setPaymentMethod("SEPAY");
        entity.setBankCode(tx.getBankCode());
        entity.setAccountNumber(tx.getAccountNumber());
        entity.setAccountName(tx.getAccountName());
        entity.setSepayReferenceCode(tx.getSepayReferenceCode());
        entity.setDescription(desc);
        entity.setItemDetailsJson(toJson(details));
        entity.setCompletedAt(tx.getCompletedAt() != null ? tx.getCompletedAt() : LocalDateTime.now());

        CashflowRecordEntity saved = cashflowRecordRepository.save(entity);
        CashflowRecordResponseDTO dto = mapToDTO(saved);

        // Bắn realtime thông báo nạp tiền thành công!
        cashflowRealtimeService.broadcastTransaction(dto);
        return dto;
    }

    @Override
    @Transactional
    public CashflowRecordResponseDTO recordSubscriptionPending(SubscriptionOrderEntity order) {
        UserDetailDTO user = getUserDetail(order.getUserId());

        String planName = order.getPlan() != null ? order.getPlan().getName() : "Gói cước";
        String planCode = order.getPlan() != null ? order.getPlan().getCode() : "SUB";
        int durationDays = order.getPlan() != null ? order.getPlan().getDurationDays() : 30;

        CashflowItemDetailsDTO details = CashflowItemDetailsDTO.builder()
                .planCode(planCode)
                .itemName(planName)
                .durationDays(durationDays)
                .build();

        String desc = String.format("Đăng ký Gói %s (%,d VNĐ / %d ngày) - Đang chờ quét QR",
                planName, order.getAmountVnd(), durationDays);

        CashflowRecordEntity entity = cashflowRecordRepository.findByTransactionCode(order.getTransactionCode())
                .orElseGet(() -> CashflowRecordEntity.builder()
                        .transactionCode(order.getTransactionCode())
                        .type("SUBSCRIPTION_PLAN")
                        .direction("IN")
                        .createdAt(order.getCreatedAt() != null ? order.getCreatedAt() : LocalDateTime.now())
                        .build());

        entity.setAmountVnd(order.getAmountVnd());
        entity.setRubyAmount(0L);
        entity.setUserId(order.getUserId());
        entity.setUserName(user != null ? user.getName() : "Người dùng #" + order.getUserId());
        entity.setUserEmail(user != null ? user.getEmail() : null);
        entity.setStatus("PENDING");
        entity.setPaymentMethod("SEPAY");
        entity.setBankCode(order.getBankCode());
        entity.setAccountNumber(order.getAccountNumber());
        entity.setAccountName(order.getAccountName());
        entity.setQrCodeUrl(order.getQrCodeUrl());
        entity.setDescription(desc);
        entity.setItemDetailsJson(toJson(details));

        CashflowRecordEntity saved = cashflowRecordRepository.save(entity);
        CashflowRecordResponseDTO dto = mapToDTO(saved);

        cashflowRealtimeService.broadcastTransaction(dto);
        return dto;
    }

    @Override
    @Transactional
    public CashflowRecordResponseDTO recordSubscriptionSuccess(SubscriptionOrderEntity order) {
        UserDetailDTO user = getUserDetail(order.getUserId());

        String planName = order.getPlan() != null ? order.getPlan().getName() : "Gói cước";
        String planCode = order.getPlan() != null ? order.getPlan().getCode() : "SUB";
        int durationDays = order.getPlan() != null ? order.getPlan().getDurationDays() : 30;

        CashflowItemDetailsDTO details = CashflowItemDetailsDTO.builder()
                .planCode(planCode)
                .itemName(planName)
                .durationDays(durationDays)
                .build();

        String desc = String.format("Đăng ký Gói %s (%,d VNĐ / %d ngày)", planName, order.getAmountVnd(), durationDays);

        CashflowRecordEntity entity = cashflowRecordRepository.findByTransactionCode(order.getTransactionCode())
                .orElseGet(() -> CashflowRecordEntity.builder()
                        .transactionCode(order.getTransactionCode())
                        .type("SUBSCRIPTION_PLAN")
                        .direction("IN")
                        .createdAt(order.getCreatedAt() != null ? order.getCreatedAt() : LocalDateTime.now())
                        .build());

        entity.setAmountVnd(order.getAmountVnd());
        entity.setRubyAmount(0L);
        entity.setUserId(order.getUserId());
        if (user != null) {
            entity.setUserName(user.getName());
            entity.setUserEmail(user.getEmail());
        }
        entity.setStatus("SUCCESS");
        entity.setPaymentMethod("SEPAY");
        entity.setBankCode(order.getBankCode());
        entity.setAccountNumber(order.getAccountNumber());
        entity.setAccountName(order.getAccountName());
        entity.setSepayReferenceCode(order.getSepayReferenceCode());
        entity.setDescription(desc);
        entity.setItemDetailsJson(toJson(details));
        entity.setCompletedAt(order.getCompletedAt() != null ? order.getCompletedAt() : LocalDateTime.now());

        CashflowRecordEntity saved = cashflowRecordRepository.save(entity);
        CashflowRecordResponseDTO dto = mapToDTO(saved);

        cashflowRealtimeService.broadcastTransaction(dto);
        return dto;
    }

    @Override
    @Transactional
    public CashflowRecordResponseDTO recordShopPurchase(Long userId, ShopItemEntity item) {
        UserDetailDTO user = getUserDetail(userId);

        String txCode = "PENTAVA-SHOP-" + System.currentTimeMillis();
        String slotStr = item.getSlot() != null ? item.getSlot() : "ITEM";

        CashflowItemDetailsDTO details = CashflowItemDetailsDTO.builder()
                .itemName(item.getName())
                .itemCode(item.getCode())
                .slot(slotStr)
                .build();

        String desc = String.format("Mua skin [%s] trong Shop bằng Ruby", item.getName());

        LocalDateTime now = LocalDateTime.now();
        CashflowRecordEntity entity = CashflowRecordEntity.builder()
                .transactionCode(txCode)
                .type("SHOP_PURCHASE")
                .direction("OUT")
                .amountVnd(0L)
                .rubyAmount(item.getPriceRuby())
                .userId(userId)
                .userName(user != null ? user.getName() : "Người dùng #" + userId)
                .userEmail(user != null ? user.getEmail() : null)
                .status("SUCCESS")
                .paymentMethod("RUBY_WALLET")
                .description(desc)
                .itemDetailsJson(toJson(details))
                .createdAt(now)
                .completedAt(now)
                .build();

        CashflowRecordEntity saved = cashflowRecordRepository.save(entity);
        CashflowRecordResponseDTO dto = mapToDTO(saved);

        cashflowRealtimeService.broadcastTransaction(dto);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashflowRecordResponseDTO> getTransactions(
            String direction, String type, String status, String searchTerm, String dateRange) {

        List<CashflowRecordEntity> list = cashflowRecordRepository.findAllByOrderByCreatedAtDesc();

        return list.stream()
                .filter(item -> {
                    if (direction != null && !direction.isBlank() && !"ALL".equalsIgnoreCase(direction)) {
                        if (!direction.equalsIgnoreCase(item.getDirection())) return false;
                    }

                    if (type != null && !type.isBlank() && !"ALL".equalsIgnoreCase(type)) {
                        if (!type.equalsIgnoreCase(item.getType())) return false;
                    }

                    if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
                        if (!status.equalsIgnoreCase(item.getStatus())) return false;
                    }

                    if (searchTerm != null && !searchTerm.isBlank()) {
                        String q = searchTerm.trim().toLowerCase();
                        boolean matchCode = item.getTransactionCode() != null && item.getTransactionCode().toLowerCase().contains(q);
                        boolean matchDesc = item.getDescription() != null && item.getDescription().toLowerCase().contains(q);
                        boolean matchUser = (item.getUserName() != null && item.getUserName().toLowerCase().contains(q))
                                || (item.getUserEmail() != null && item.getUserEmail().toLowerCase().contains(q))
                                || String.valueOf(item.getUserId()).equals(q);
                        boolean matchRef = item.getSepayReferenceCode() != null && item.getSepayReferenceCode().toLowerCase().contains(q);
                        if (!matchCode && !matchDesc && !matchUser && !matchRef) return false;
                    }

                    if (dateRange != null && !dateRange.isBlank() && !"ALL".equalsIgnoreCase(dateRange)) {
                        LocalDateTime itemTime = item.getCreatedAt();
                        LocalDateTime now = LocalDateTime.now();
                        if ("TODAY".equalsIgnoreCase(dateRange)) {
                            LocalDate today = LocalDate.now();
                            if (itemTime.toLocalDate().isBefore(today)) return false;
                        } else if ("7DAYS".equalsIgnoreCase(dateRange)) {
                            if (itemTime.isBefore(now.minusDays(7))) return false;
                        } else if ("30DAYS".equalsIgnoreCase(dateRange)) {
                            if (itemTime.isBefore(now.minusDays(30))) return false;
                        }
                    }

                    return true;
                })
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CashflowSummaryResponseDTO getCashflowSummary() {
        List<CashflowRecordEntity> all = cashflowRecordRepository.findAll();

        long totalRevenueVnd = 0;
        long todayRevenueVnd = 0;
        long pendingRevenueVnd = 0;
        long successCount = 0;
        long pendingCount = 0;
        long failedCount = 0;
        long rubyMinted = 0;
        long rubySpent = 0;
        long rubyTopupVnd = 0;
        long subscriptionVnd = 0;

        LocalDate today = LocalDate.now();

        for (CashflowRecordEntity item : all) {
            if ("SUCCESS".equalsIgnoreCase(item.getStatus())) {
                successCount++;
                if ("IN".equalsIgnoreCase(item.getDirection())) {
                    totalRevenueVnd += item.getAmountVnd();
                    if (item.getCompletedAt() != null && item.getCompletedAt().toLocalDate().isEqual(today)) {
                        todayRevenueVnd += item.getAmountVnd();
                    } else if (item.getCreatedAt() != null && item.getCreatedAt().toLocalDate().isEqual(today)) {
                        todayRevenueVnd += item.getAmountVnd();
                    }

                    if ("TOPUP_RUBY".equalsIgnoreCase(item.getType())) {
                        rubyTopupVnd += item.getAmountVnd();
                        rubyMinted += item.getRubyAmount();
                    } else if ("SUBSCRIPTION_PLAN".equalsIgnoreCase(item.getType())) {
                        subscriptionVnd += item.getAmountVnd();
                    }
                } else if ("OUT".equalsIgnoreCase(item.getDirection())) {
                    if ("SHOP_PURCHASE".equalsIgnoreCase(item.getType())) {
                        rubySpent += item.getRubyAmount();
                    }
                }
            } else if ("PENDING".equalsIgnoreCase(item.getStatus())) {
                pendingCount++;
                if ("IN".equalsIgnoreCase(item.getDirection())) {
                    pendingRevenueVnd += item.getAmountVnd();
                }
            } else {
                failedCount++;
            }
        }

        long totalTransactions = all.size();
        double conversionRate = totalTransactions > 0
                ? Math.round(((double) successCount / totalTransactions) * 1000.0) / 10.0
                : 100.0;

        long rubyCirculating = 0;
        try {
            rubyCirculating = userWalletRepository.getTotalCirculatingRuby();
        } catch (Exception e) {
            log.warn("Không thể tính tổng Ruby đang lưu hành: {}", e.getMessage());
            rubyCirculating = Math.max(0, rubyMinted - rubySpent);
        }

        return CashflowSummaryResponseDTO.builder()
                .totalRevenueVnd(totalRevenueVnd)
                .todayRevenueVnd(todayRevenueVnd)
                .pendingRevenueVnd(pendingRevenueVnd)
                .totalTransactions(totalTransactions)
                .successCount(successCount)
                .pendingCount(pendingCount)
                .failedCount(failedCount)
                .rubyMinted(rubyMinted)
                .rubySpent(rubySpent)
                .rubyCirculating(rubyCirculating)
                .conversionRate(conversionRate)
                .revenueBySource(CashflowSummaryResponseDTO.RevenueBySourceDTO.builder()
                        .rubyTopupVnd(rubyTopupVnd)
                        .subscriptionVnd(subscriptionVnd)
                        .build())
                .build();
    }

    @Override
    @Transactional
    public CashflowRecordResponseDTO approveTransaction(String transactionCode) {
        if (transactionCode == null || transactionCode.isBlank()) {
            throw new IllegalArgumentException("Mã giao dịch không hợp lệ");
        }

        String finalCode = transactionCode.trim().toUpperCase();
        CashflowRecordEntity record = cashflowRecordRepository.findByTransactionCode(finalCode)
                .orElse(null);

        // Trường hợp giao dịch Topup
        Optional<TopupTransactionEntity> topupOpt = topupTransactionRepository.findByTransactionCode(finalCode);
        if (topupOpt.isPresent()) {
            shopWalletInputPort.mockConfirmTopup(finalCode);
            TopupTransactionEntity tx = topupTransactionRepository.findByTransactionCode(finalCode).orElseThrow();
            return recordTopupSuccess(tx);
        }

        // Trường hợp giao dịch Mua gói cước Subscription
        Optional<SubscriptionOrderEntity> subOpt = subscriptionOrderRepository.findByTransactionCode(finalCode);
        if (subOpt.isPresent()) {
            subscriptionInputPort.mockConfirmPurchase(finalCode);
            SubscriptionOrderEntity order = subscriptionOrderRepository.findByTransactionCode(finalCode).orElseThrow();
            return recordSubscriptionSuccess(order);
        }

        if (record == null) {
            throw new IllegalArgumentException("Không tìm thấy giao dịch: " + finalCode);
        }

        record.setStatus("SUCCESS");
        record.setCompletedAt(LocalDateTime.now());
        CashflowRecordEntity saved = cashflowRecordRepository.save(record);
        CashflowRecordResponseDTO dto = mapToDTO(saved);
        cashflowRealtimeService.broadcastTransaction(dto);
        return dto;
    }

    private void syncHistoricalData() {
        // 1. Đồng bộ topup_transactions
        List<TopupTransactionEntity> topups = topupTransactionRepository.findAll();
        for (TopupTransactionEntity t : topups) {
            if (cashflowRecordRepository.findByTransactionCode(t.getTransactionCode()).isEmpty()) {
                UserDetailDTO u = getUserDetail(t.getUserId());
                String desc = t.getPackageName() != null
                        ? String.format("Nạp %s (%d Ruby = %,d VNĐ)", t.getPackageName(), t.getRubyAmount(), t.getAmountVnd())
                        : String.format("Nạp lẻ %d Ruby = %,d VNĐ", t.getRubyAmount(), t.getAmountVnd());
                CashflowItemDetailsDTO details = CashflowItemDetailsDTO.builder()
                        .itemCode(t.getPackageCode())
                        .itemName(t.getPackageName())
                        .build();

                cashflowRecordRepository.save(CashflowRecordEntity.builder()
                        .transactionCode(t.getTransactionCode())
                        .type("TOPUP_RUBY")
                        .direction("IN")
                        .amountVnd(t.getAmountVnd())
                        .rubyAmount(t.getRubyAmount())
                        .userId(t.getUserId())
                        .userName(u != null ? u.getName() : "Người dùng #" + t.getUserId())
                        .userEmail(u != null ? u.getEmail() : null)
                        .status(t.getStatus() != null ? t.getStatus().name() : "PENDING")
                        .paymentMethod("SEPAY")
                        .bankCode(t.getBankCode())
                        .accountNumber(t.getAccountNumber())
                        .accountName(t.getAccountName())
                        .sepayReferenceCode(t.getSepayReferenceCode())
                        .description(desc)
                        .itemDetailsJson(toJson(details))
                        .createdAt(t.getCreatedAt())
                        .completedAt(t.getCompletedAt())
                        .build());
            }
        }

        // 2. Đồng bộ subscription_orders
        List<SubscriptionOrderEntity> subs = subscriptionOrderRepository.findAll();
        for (SubscriptionOrderEntity s : subs) {
            if (cashflowRecordRepository.findByTransactionCode(s.getTransactionCode()).isEmpty()) {
                UserDetailDTO u = getUserDetail(s.getUserId());
                String pName = s.getPlan() != null ? s.getPlan().getName() : "Gói cước";
                String pCode = s.getPlan() != null ? s.getPlan().getCode() : "SUB";
                int dur = s.getPlan() != null ? s.getPlan().getDurationDays() : 30;

                CashflowItemDetailsDTO details = CashflowItemDetailsDTO.builder()
                        .planCode(pCode)
                        .itemName(pName)
                        .durationDays(dur)
                        .build();

                cashflowRecordRepository.save(CashflowRecordEntity.builder()
                        .transactionCode(s.getTransactionCode())
                        .type("SUBSCRIPTION_PLAN")
                        .direction("IN")
                        .amountVnd(s.getAmountVnd())
                        .rubyAmount(0L)
                        .userId(s.getUserId())
                        .userName(u != null ? u.getName() : "Người dùng #" + s.getUserId())
                        .userEmail(u != null ? u.getEmail() : null)
                        .status(s.getStatus() != null ? s.getStatus().name() : "PENDING")
                        .paymentMethod("SEPAY")
                        .bankCode(s.getBankCode())
                        .accountNumber(s.getAccountNumber())
                        .accountName(s.getAccountName())
                        .sepayReferenceCode(s.getSepayReferenceCode())
                        .qrCodeUrl(s.getQrCodeUrl())
                        .description(String.format("Đăng ký Gói %s (%,d VNĐ / %d ngày)", pName, s.getAmountVnd(), dur))
                        .itemDetailsJson(toJson(details))
                        .createdAt(s.getCreatedAt())
                        .completedAt(s.getCompletedAt())
                        .build());
            }
        }

        // 3. Đồng bộ purchase_transactions
        List<PurchaseTransactionEntity> purchases = purchaseTransactionRepository.findAll();
        for (PurchaseTransactionEntity p : purchases) {
            String code = "PENTAVA-SHOP-" + p.getId();
            if (cashflowRecordRepository.findByTransactionCode(code).isEmpty()) {
                UserDetailDTO u = getUserDetail(p.getUserId());
                Optional<ShopItemEntity> itemOpt = shopItemRepository.findBySkinItemId(p.getSkinItemId());
                String itemName = itemOpt.map(ShopItemEntity::getName).orElse("Vật phẩm #" + p.getSkinItemId());
                String slot = itemOpt.map(i -> i.getSlot() != null ? i.getSlot() : "ITEM").orElse("ITEM");

                CashflowItemDetailsDTO details = CashflowItemDetailsDTO.builder()
                        .itemName(itemName)
                        .slot(slot)
                        .build();

                cashflowRecordRepository.save(CashflowRecordEntity.builder()
                        .transactionCode(code)
                        .type("SHOP_PURCHASE")
                        .direction("OUT")
                        .amountVnd(0L)
                        .rubyAmount(p.getPriceRuby())
                        .userId(p.getUserId())
                        .userName(u != null ? u.getName() : "Người dùng #" + p.getUserId())
                        .userEmail(u != null ? u.getEmail() : null)
                        .status("SUCCESS")
                        .paymentMethod("RUBY_WALLET")
                        .description(String.format("Mua skin [%s] trong Shop bằng Ruby", itemName))
                        .itemDetailsJson(toJson(details))
                        .createdAt(p.getPurchasedAt() != null ? p.getPurchasedAt() : LocalDateTime.now())
                        .completedAt(p.getPurchasedAt())
                        .build());
            }
        }

        log.info("Đã đồng bộ xong dữ liệu lịch sử vào sổ quỹ dòng tiền. Tổng số bản ghi hiện tại: {}",
                cashflowRecordRepository.count());
    }

    private UserDetailDTO getUserDetail(Long userId) {
        if (userId == null) return null;
        return userCache.computeIfAbsent(userId, id ->
                authServiceGateway.getUserById(id).orElse(null)
        );
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private CashflowItemDetailsDTO parseJson(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, CashflowItemDetailsDTO.class);
        } catch (Exception e) {
            return null;
        }
    }

    private CashflowRecordResponseDTO mapToDTO(CashflowRecordEntity entity) {
        DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return CashflowRecordResponseDTO.builder()
                .id(entity.getId())
                .transactionCode(entity.getTransactionCode())
                .type(entity.getType())
                .direction(entity.getDirection())
                .amountVnd(entity.getAmountVnd())
                .rubyAmount(entity.getRubyAmount())
                .userId(entity.getUserId())
                .userName(entity.getUserName())
                .userEmail(entity.getUserEmail())
                .status(entity.getStatus())
                .paymentMethod(entity.getPaymentMethod())
                .bankCode(entity.getBankCode())
                .accountNumber(entity.getAccountNumber())
                .accountName(entity.getAccountName())
                .sepayReferenceCode(entity.getSepayReferenceCode())
                .qrCodeUrl(entity.getQrCodeUrl())
                .description(entity.getDescription())
                .itemDetails(parseJson(entity.getItemDetailsJson()))
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().format(dtf) : null)
                .completedAt(entity.getCompletedAt() != null ? entity.getCompletedAt().format(dtf) : null)
                .build();
    }
}
