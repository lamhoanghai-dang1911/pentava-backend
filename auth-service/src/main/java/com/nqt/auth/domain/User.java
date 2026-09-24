package com.nqt.auth.domain;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class User {
    private Long id;
    private String email;
    private String password;
    private String name;
    private String authProvider;
    private String googleId; // ➕ sub claim từ Google, null nếu account LOCAL
    private String otpCode;
    private LocalDateTime otpExpiryTime;
    private boolean isVerified;
    private boolean hasCompletedOnboarding;
    private String avatarUrl;
    private String bio;
    private LocalDateTime createdAt;

    // ➕ Quên mật khẩu
    private String resetOtpCode;
    private LocalDateTime resetOtpExpiryTime;
    @Builder.Default
    private int resetOtpAttempts = 0;
    private LocalDateTime resetOtpLockedUntil;
    private String resetToken;
    private LocalDateTime resetTokenExpiryTime;
}