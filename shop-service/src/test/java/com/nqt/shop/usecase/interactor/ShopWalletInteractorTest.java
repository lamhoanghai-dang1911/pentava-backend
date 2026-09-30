package com.nqt.shop.usecase.interactor;

import com.nqt.shop.domain.model.RubyPackage;
import com.nqt.shop.domain.model.TopupStatus;
import com.nqt.shop.infrastructure.persistence.entity.TopupTransactionEntity;
import com.nqt.shop.infrastructure.persistence.entity.UserWalletEntity;
import com.nqt.shop.infrastructure.persistence.repository.TopupTransactionRepository;
import com.nqt.shop.infrastructure.persistence.repository.UserWalletRepository;
import com.nqt.shop.usecase.dto.RubyTopupPricingResponseDTO;
import com.nqt.shop.usecase.dto.TopupInitRequestDTO;
import com.nqt.shop.usecase.dto.TopupInitResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShopWalletInteractorTest {

    @Mock
    private UserWalletRepository userWalletRepository;

    @Mock
    private TopupTransactionRepository topupTransactionRepository;

    @InjectMocks
    private ShopWalletInteractor shopWalletInteractor;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(shopWalletInteractor, "sepayApiKey", "test-key");
        ReflectionTestUtils.setField(shopWalletInteractor, "sepayBankCode", "MBBank");
        ReflectionTestUtils.setField(shopWalletInteractor, "sepayAccountNumber", "0123456789");
        ReflectionTestUtils.setField(shopWalletInteractor, "sepayAccountName", "PENTAVA");
    }

    @Test
    void testGetTopupPricing() {
        RubyTopupPricingResponseDTO pricing = shopWalletInteractor.getTopupPricing();
        assertNotNull(pricing);
        assertEquals(2000L, pricing.getBaseRateVndPerRuby());
        assertEquals(20000L, pricing.getMinCustomTopupVnd());
        assertEquals(10L, pricing.getMinCustomTopupRuby());
        assertEquals(3, pricing.getPackages().size());

        // Small Pack
        assertEquals("SMALL", pricing.getPackages().get(0).getCode());
        assertEquals(50L, pricing.getPackages().get(0).getRubyAmount());
        assertEquals(100000L, pricing.getPackages().get(0).getPriceVnd());

        // Medium Pack
        assertEquals("MEDIUM", pricing.getPackages().get(1).getCode());
        assertEquals(120L, pricing.getPackages().get(1).getRubyAmount());
        assertEquals(220000L, pricing.getPackages().get(1).getPriceVnd());
        assertEquals(20000L, pricing.getPackages().get(1).getDiscountVnd());

        // Large Pack
        assertEquals("LARGE", pricing.getPackages().get(2).getCode());
        assertEquals(300L, pricing.getPackages().get(2).getRubyAmount());
        assertEquals(540000L, pricing.getPackages().get(2).getPriceVnd());
        assertEquals(60000L, pricing.getPackages().get(2).getDiscountVnd());
    }

    @Test
    void testInitTopupSmallPack() {
        TopupInitRequestDTO req = TopupInitRequestDTO.builder()
                .packageCode("SMALL")
                .build();

        TopupInitResponseDTO resp = shopWalletInteractor.initTopup(1L, req);

        assertEquals(100000L, resp.getAmountVnd());
        assertEquals(50L, resp.getRubyAmount());
        assertEquals("SMALL", resp.getPackageCode());
        assertEquals("Small Pack", resp.getPackageName());
        assertEquals(TopupStatus.PENDING, resp.getStatus());
        verify(topupTransactionRepository, times(1)).save(any(TopupTransactionEntity.class));
    }

    @Test
    void testInitTopupMediumPack() {
        TopupInitRequestDTO req = TopupInitRequestDTO.builder()
                .packageCode("MEDIUM")
                .build();

        TopupInitResponseDTO resp = shopWalletInteractor.initTopup(1L, req);

        assertEquals(220000L, resp.getAmountVnd());
        assertEquals(120L, resp.getRubyAmount());
        assertEquals("MEDIUM", resp.getPackageCode());
        assertEquals("Medium Pack", resp.getPackageName());
    }

    @Test
    void testInitTopupLargePack() {
        TopupInitRequestDTO req = TopupInitRequestDTO.builder()
                .packageCode("LARGE")
                .build();

        TopupInitResponseDTO resp = shopWalletInteractor.initTopup(1L, req);

        assertEquals(540000L, resp.getAmountVnd());
        assertEquals(300L, resp.getRubyAmount());
        assertEquals("LARGE", resp.getPackageCode());
        assertEquals("Large Pack", resp.getPackageName());
    }

    @Test
    void testInitTopupCustomAmountVnd() {
        // Nạp lẻ 40.000 VNĐ -> 20 Ruby
        TopupInitRequestDTO req = TopupInitRequestDTO.builder()
                .amountVnd(40000L)
                .build();

        TopupInitResponseDTO resp = shopWalletInteractor.initTopup(1L, req);

        assertEquals(40000L, resp.getAmountVnd());
        assertEquals(20L, resp.getRubyAmount());
        assertEquals("CUSTOM", resp.getPackageCode());
        assertEquals("Nạp lẻ", resp.getPackageName());
    }

    @Test
    void testInitTopupCustomRubyAmount() {
        // Nạp lẻ 15 Ruby -> 30.000 VNĐ
        TopupInitRequestDTO req = TopupInitRequestDTO.builder()
                .rubyAmount(15L)
                .build();

        TopupInitResponseDTO resp = shopWalletInteractor.initTopup(1L, req);

        assertEquals(30000L, resp.getAmountVnd());
        assertEquals(15L, resp.getRubyAmount());
        assertEquals("CUSTOM", resp.getPackageCode());
    }

    @Test
    void testInitTopupCustomBelowMinVnd() {
        TopupInitRequestDTO req = TopupInitRequestDTO.builder()
                .amountVnd(18000L)
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> shopWalletInteractor.initTopup(1L, req));
        assertTrue(ex.getMessage().contains("tối thiểu là 20.000 VNĐ"));
    }

    @Test
    void testInitTopupCustomNotMultipleOf2000() {
        TopupInitRequestDTO req = TopupInitRequestDTO.builder()
                .amountVnd(25000L)
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> shopWalletInteractor.initTopup(1L, req));
        assertTrue(ex.getMessage().contains("bội số của 2.000 VNĐ"));
    }

    @Test
    void testInitTopupInvalidPackageCode() {
        TopupInitRequestDTO req = TopupInitRequestDTO.builder()
                .packageCode("SUPER_VIP")
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> shopWalletInteractor.initTopup(1L, req));
        assertTrue(ex.getMessage().contains("không hợp lệ"));
    }

    @Test
    void testMockConfirmTopup() {
        TopupTransactionEntity tx = TopupTransactionEntity.builder()
                .id(1L)
                .userId(100L)
                .transactionCode("PENTAVA123456")
                .amountVnd(220000L)
                .rubyAmount(120L)
                .status(TopupStatus.PENDING)
                .build();

        UserWalletEntity wallet = UserWalletEntity.builder()
                .id(10L)
                .userId(100L)
                .rubyBalance(50)
                .build();

        when(topupTransactionRepository.findByTransactionCode("PENTAVA123456"))
                .thenReturn(Optional.of(tx));
        when(userWalletRepository.findByUserId(100L))
                .thenReturn(Optional.of(wallet));

        shopWalletInteractor.mockConfirmTopup("PENTAVA123456");

        assertEquals(TopupStatus.SUCCESS, tx.getStatus());
        assertEquals(170, wallet.getRubyBalance());
        verify(userWalletRepository, times(1)).save(wallet);
    }
}
