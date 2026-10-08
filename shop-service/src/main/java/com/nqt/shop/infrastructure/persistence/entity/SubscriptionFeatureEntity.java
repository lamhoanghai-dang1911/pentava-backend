package com.nqt.shop.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_features")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionFeatureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "feature_code", nullable = false, unique = true, length = 64)
    private String featureCode; // STORAGE_EXPANSION, STREAK_PROTECTION, MULTI_ANGLE_TEMPLATES, CUSTOM_ROADMAP, UNLOCK_ALL_SKINS, RUBY_BONUS

    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "default_value", length = 64)
    private String defaultValue; // ví dụ: "5GB", "2", "true", "50"

    @Column(length = 32)
    private String unit; // "GB", "lần/tháng", "Ruby", "boolean"

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
