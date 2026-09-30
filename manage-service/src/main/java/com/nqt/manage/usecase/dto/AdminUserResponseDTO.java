package com.nqt.manage.usecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponseDTO {
    private Long id;
    private String email;
    /**
     * Tên người dùng lấy từ onboarding-service (KHÔNG lấy từ auth-service)
     */
    private String name;
    private String avatarUrl;
    private String bio;
    private Boolean isVerified;
    private Boolean hasCompletedOnboarding;
}
