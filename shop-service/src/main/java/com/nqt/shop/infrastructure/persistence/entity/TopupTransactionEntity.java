package com.nqt.shop.infrastructure.persistence.entity;

import com.nqt.shop.domain.model.TopupStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "topup_transactions", uniqueConstraints = {
        @UniqueConstraint(columnNames = "transaction_code")
}, indexes = {
        @Index(name = "idx_topup_user_id", columnList = "user_id"),
        @Index(name = "idx_topup_tx_code", columnList = "transaction_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopupTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "transaction_code", nullable = false, length = 64)
    private String transactionCode;

    @Column(name = "amount_vnd", nullable = false)
    private long amountVnd;

    @Column(name = "ruby_amount", nullable = false)
    private long rubyAmount;

    @Column(name = "package_code", length = 32)
    private String packageCode;

    @Column(name = "package_name", length = 128)
    private String packageName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TopupStatus status;

    @Column(name = "payment_method", length = 32)
    private String paymentMethod;

    @Column(name = "bank_code", length = 32)
    private String bankCode;

    @Column(name = "account_number", length = 64)
    private String accountNumber;

    @Column(name = "account_name", length = 128)
    private String accountName;

    @Column(name = "sepay_reference_code", length = 128)
    private String sepayReferenceCode;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
