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
    private String otpCode;
    private LocalDateTime otpExpiryTime;
    private boolean isVerified;
    private boolean hasCompletedOnboarding;
    private LocalDateTime createdAt;
}