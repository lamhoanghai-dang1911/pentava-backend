package com.nqt.shop.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum RubyPackage {
    SMALL("SMALL", "Small Pack", 50, 100_000L, 0L, 0L, "Small Pack (50 ruby) = 100.000 VNĐ", false, false),
    MEDIUM("MEDIUM", "Medium Pack", 120, 220_000L, 20_000L, 10L, "Medium Pack (120 ruby) = 220.000 VNĐ (Tiết kiệm 20.000 VNĐ)", true, false),
    LARGE("LARGE", "Large Pack", 300, 540_000L, 60_000L, 30L, "Large Pack (300 ruby) = 540.000 VNĐ (Tiết kiệm 60.000 VNĐ)", false, true);

    private final String code;
    private final String name;
    private final long rubyAmount;
    private final long priceVnd;
    private final long discountVnd;
    private final long bonusRuby;
    private final String description;
    private final boolean isPopular;
    private final boolean isBestValue;

    public static Optional<RubyPackage> fromCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String normalized = code.trim().toUpperCase()
                .replace("-", "_")
                .replace(" ", "_")
                .replace("_PACK", "");
        return Arrays.stream(values())
                .filter(p -> p.name().equalsIgnoreCase(normalized) || p.getCode().equalsIgnoreCase(normalized))
                .findFirst();
    }

    public static Optional<RubyPackage> fromAmountVnd(Long amountVnd) {
        if (amountVnd == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(p -> p.getPriceVnd() == amountVnd)
                .findFirst();
    }

    public static Optional<RubyPackage> fromRubyAmount(Long rubyAmount) {
        if (rubyAmount == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(p -> p.getRubyAmount() == rubyAmount)
                .findFirst();
    }
}
