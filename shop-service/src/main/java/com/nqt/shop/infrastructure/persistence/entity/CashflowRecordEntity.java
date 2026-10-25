package com.nqt.shop.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cashflow_records", indexes = {
        @Index(name = "idx_cf_tx_code", columnList = "transaction_code"),
        @Index(name = "idx_cf_user_id", columnList = "user_id"),
        @Index(name = "idx_cf_created_at", columnList = "created_at"),
        @Index(name = "idx_cf_direction", columnList = "direction"),
        @Index(name = "idx_cf_type", columnList = "type"),
        @Index(name = "idx_cf_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashflowRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_code", nullable = false, length = 64)
    private String transactionCode;

    @Column(name = "type", nullable = false, length = 32)
    private String type; // TOPUP_RUBY, SUBSCRIPTION_PLAN, SHOP_PURCHASE

    @Column(name = "direction", nullable = false, length = 8)
    private String direction; // IN, OUT

    @Column(name = "amount_vnd", nullable = false)
    private long amountVnd;

    @Column(name = "ruby_amount", nullable = false)
    private long rubyAmount;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_name", length = 128)
    private String userName;

    @Column(name = "user_email", length = 128)
    private String userEmail;

    @Column(name = "status", nullable = false, length = 32)
    private String status; // SUCCESS, PENDING, FAILED

    @Column(name = "payment_method", length = 32)
    private String paymentMethod; // SEPAY, RUBY_WALLET, MANUAL

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

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "item_details_json", columnDefinition = "TEXT")
    private String itemDetailsJson;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
