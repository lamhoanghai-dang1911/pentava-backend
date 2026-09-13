package com.nqt.auth.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String name;

    private String authProvider;

    private String otpCode;

    private LocalDateTime otpExpiryTime;

    @Builder.Default
    private boolean isVerified = false;

    // 🚀 BỔ SUNG CỘT NÀY ĐỂ HIBERNATE TỰ ĐỘNG TẠO TRÊN DATABASE
    @Builder.Default
    @Column(name = "has_completed_onboarding", nullable = false)
    private boolean hasCompletedOnboarding = false;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}