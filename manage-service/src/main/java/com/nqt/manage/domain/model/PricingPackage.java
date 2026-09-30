package com.nqt.manage.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingPackage {
    private Long id;
    private String name;
    private Integer rubyAmount;
    private Integer bonusRuby;
    private BigDecimal priceVnd;
    private String tag;
    private String features; // JSON string or comma-separated list of perks
    private String ctaText;
    private String ctaLink;
    private Integer displayOrder;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
