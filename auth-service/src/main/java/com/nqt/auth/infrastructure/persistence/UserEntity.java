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

    @Column(name = "google_id", unique = true)
    private String googleId; // ➕ sub claim từ Google

    private String otpCode;

    private LocalDateTime otpExpiryTime;

    @Builder.Default
    private boolean isVerified = false;

    @Builder.Default
    @Column(name = "has_completed_onboarding", nullable = false)
    private boolean hasCompletedOnboarding = false;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "bio", length = 1000)
    private String bio;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // ➕ Quên mật khẩu
    @Column(name = "reset_otp_code")
    private String resetOtpCode;

    @Column(name = "reset_otp_expiry_time")
    private LocalDateTime resetOtpExpiryTime;

    @Builder.Default
    @Column(name = "reset_otp_attempts", nullable = false)
    private int resetOtpAttempts = 0;

    @Column(name = "reset_otp_locked_until")
    private LocalDateTime resetOtpLockedUntil;

    @Column(name = "reset_token", unique = true)
    private String resetToken;

    @Column(name = "reset_token_expiry_time")
    private LocalDateTime resetTokenExpiryTime;
}