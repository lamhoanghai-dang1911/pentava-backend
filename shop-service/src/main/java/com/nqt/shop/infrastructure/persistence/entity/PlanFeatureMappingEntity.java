package com.nqt.shop.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "plan_feature_mappings", uniqueConstraints = {
        @UniqueConstraint(name = "uk_plan_feature", columnNames = {"plan_id", "feature_id"})
}, indexes = {
        @Index(name = "idx_mapping_plan_id", columnList = "plan_id"),
        @Index(name = "idx_mapping_feature_id", columnList = "feature_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanFeatureMappingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlanEntity plan;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "feature_id", nullable = false)
    private SubscriptionFeatureEntity feature;

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled; // Admin bật hoặc tắt tính năng này trong gói

    @Column(name = "param_value", length = 128)
    private String paramValue; // Giá trị tham số, ví dụ: "5GB", "2", "50", "true"

    @Column(name = "display_label", length = 255)
    private String displayLabel; // Câu văn hiển thị cho người dùng xem

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
