package com.nqt.manage.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "landing_pricing_packages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingPackageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer rubyAmount;

    @Builder.Default
    private Integer bonusRuby = 0;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal priceVnd;

    private String tag;

    @Column(columnDefinition = "TEXT")
    private String features;

    private String ctaText;
    private String ctaLink;

    @Builder.Default
    private Integer displayOrder = 0;

    @Builder.Default
    private Boolean isActive = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
