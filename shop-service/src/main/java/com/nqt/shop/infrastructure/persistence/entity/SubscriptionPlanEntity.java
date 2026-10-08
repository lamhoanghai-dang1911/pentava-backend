package com.nqt.shop.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subscription_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String code; // BASIC, GOLD, PREMIUM

    @Column(nullable = false, length = 128)
    private String name; // Gói Basic, Gói Gold, Gói Premium

    @Column(name = "price_vnd", nullable = false)
    private long priceVnd; // 39000, 59000, 149000

    @Column(name = "duration_days", nullable = false)
    private int durationDays; // 30 ngày

    @Column(length = 500)
    private String description;

    @Column(length = 64)
    private String badge; // "Phổ biến nhất", "VIP", ...

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PlanFeatureMappingEntity> featureMappings = new ArrayList<>();
}
