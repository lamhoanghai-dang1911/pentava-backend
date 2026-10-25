package com.nqt.shop.usecase.interactor;

import com.nqt.shop.domain.exception.InsufficientRubyException;
import com.nqt.shop.domain.exception.ItemAlreadyOwnedException;
import com.nqt.shop.domain.exception.ShopItemNotFoundException;
import com.nqt.shop.infrastructure.persistence.entity.PurchaseTransactionEntity;
import com.nqt.shop.infrastructure.persistence.entity.ShopItemEntity;
import com.nqt.shop.infrastructure.persistence.entity.UserWalletEntity;
import com.nqt.shop.infrastructure.persistence.repository.PurchaseTransactionRepository;
import com.nqt.shop.infrastructure.persistence.repository.ShopItemRepository;
import com.nqt.shop.infrastructure.persistence.repository.UserWalletRepository;
import com.nqt.shop.usecase.boundary.ShopPurchaseInputPort;
import com.nqt.shop.usecase.boundary.SkinServiceGateway;
import com.nqt.shop.usecase.dto.BuyItemResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopPurchaseInteractor implements ShopPurchaseInputPort {

    private final ShopItemRepository shopItemRepository;
    private final UserWalletRepository userWalletRepository;
    private final PurchaseTransactionRepository purchaseTransactionRepository;
    private final SkinServiceGateway skinServiceGateway;
    @org.springframework.context.annotation.Lazy
    private final com.nqt.shop.usecase.boundary.CashflowInputPort cashflowInputPort;

    @Override
    @Transactional
    public BuyItemResponseDTO buyItem(Long userId, Long skinItemId) {
        ShopItemEntity item = shopItemRepository.findBySkinItemId(skinItemId)
                .orElseGet(() -> shopItemRepository.findById(skinItemId)
                        .orElseThrow(() -> new ShopItemNotFoundException("Không tìm thấy skin trong cửa hàng với ID: " + skinItemId)));

        if (!item.isForSale()) {
            throw new IllegalStateException("Vật phẩm hiện không được mở bán");
        }

        // Kiểm tra xem đã sở hữu chưa
        boolean alreadyOwned = skinServiceGateway.checkSkinOwned(userId, item.getSkinItemId());
        if (alreadyOwned) {
            throw new ItemAlreadyOwnedException("Bạn đã sở hữu skin '" + item.getName() + "' rồi!");
        }

        // Kiểm tra số dư ví
        UserWalletEntity wallet = userWalletRepository.findByUserId(userId)
                .orElseGet(() -> UserWalletEntity.builder()
                        .userId(userId)
                        .rubyBalance(0)
                        .updatedAt(LocalDateTime.now())
                        .build());

        if (wallet.getRubyBalance() < item.getPriceRuby()) {
            throw new InsufficientRubyException(String.format(
                    "Số dư Ruby không đủ. Bạn cần %d Ruby nhưng hiện tại chỉ có %d Ruby.",
                    item.getPriceRuby(), wallet.getRubyBalance()
            ));
        }

        // Trừ tiền trong ví
        wallet.setRubyBalance(wallet.getRubyBalance() - item.getPriceRuby());
        wallet.setUpdatedAt(LocalDateTime.now());
        userWalletRepository.save(wallet);

        // Lưu lịch sử giao dịch mua đồ
        PurchaseTransactionEntity purchase = PurchaseTransactionEntity.builder()
                .userId(userId)
                .skinItemId(item.getSkinItemId())
                .priceRuby(item.getPriceRuby())
                .purchasedAt(LocalDateTime.now())
                .build();
        purchaseTransactionRepository.save(purchase);

        // Cấp quyền sở hữu skin sang skin-service qua gRPC
        boolean granted = skinServiceGateway.grantSkin(userId, item.getSkinItemId());
        if (!granted) {
            log.error("Cấp skin sang skin-service thất bại cho userId: {}, skinId: {}", userId, item.getSkinItemId());
            throw new IllegalStateException("Hệ thống không thể cấp skin vào kho đồ, vui lòng liên hệ admin hỗ trợ");
        }

        log.info("User {} bought skin {} ({}) for {} Ruby. Remaining: {}",
                userId, item.getSkinItemId(), item.getName(), item.getPriceRuby(), wallet.getRubyBalance());

        // Ghi nhận vào dòng tiền chi tiêu (OUT) và bắn realtime
        try {
            cashflowInputPort.recordShopPurchase(userId, item);
        } catch (Exception e) {
            log.warn("Không thể ghi nhận cashflow purchase: {}", e.getMessage());
        }

        return BuyItemResponseDTO.builder()
                .success(true)
                .message("Mua skin '" + item.getName() + "' thành công!")
                .skinItemId(item.getSkinItemId())
                .skinName(item.getName())
                .rubyDeducted(item.getPriceRuby())
                .remainingRuby(wallet.getRubyBalance())
                .build();
    }
}
