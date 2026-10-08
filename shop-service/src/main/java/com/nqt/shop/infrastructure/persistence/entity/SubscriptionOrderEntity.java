package com.nqt.shop.infrastructure.persistence.entity;

import com.nqt.shop.domain.model.TopupStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_orders", uniqueConstraints = {
        @UniqueConstraint(name = "uk_sub_order_code", columnNames = "transaction_code")
}, indexes = {
        @Index(name = "idx_sub_order_user", columnList = "user_id"),
        @Index(name = "idx_sub_order_code", columnList = "transaction_code"),
        @Index(name = "idx_sub_order_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlanEntity plan;

    @Column(name = "transaction_code", nullable = false, length = 64)
    private String transactionCode; // Ví dụ: PENTAVA829103

    @Column(name = "amount_vnd", nullable = false)
    private long amountVnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TopupStatus status; // PENDING, SUCCESS, FAILED

    @Column(name = "payment_method", length = 32)
    private String paymentMethod; // SEPAY

    @Column(name = "bank_code", length = 32)
    private String bankCode;

    @Column(name = "account_number", length = 64)
    private String accountNumber;

    @Column(name = "account_name", length = 128)
    private String accountName;

    @Column(name = "sepay_reference_code", length = 128)
    private String sepayReferenceCode;

    @Column(name = "qr_code_url", length = 1000)
    private String qrCodeUrl;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
