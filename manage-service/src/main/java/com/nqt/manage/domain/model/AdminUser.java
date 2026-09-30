package com.nqt.manage.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUser {
    private Long id;
    private String email;
    /**
     * Tên người dùng LẤY TỪ ONBOARDING-SERVICE, không dùng từ auth-service
     */
    private String name;
    private String avatarUrl;
    private String bio;
    private Boolean isVerified;
    private Boolean hasCompletedOnboarding;
}
