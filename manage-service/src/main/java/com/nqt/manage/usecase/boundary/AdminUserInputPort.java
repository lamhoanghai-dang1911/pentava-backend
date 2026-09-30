package com.nqt.manage.usecase.boundary;

import com.nqt.manage.usecase.dto.AdminUserResponseDTO;
import com.nqt.manage.usecase.dto.UpdateUserNameRequestDTO;

public interface AdminUserInputPort {
    /**
     * Lấy thông tin người dùng kết hợp:
     * - auth-service (email, avatar, bio, isVerified, hasCompletedOnboarding)
     * - onboarding-service (lấy trường name thông qua gRPC OnboardingProfileService)
     */
    AdminUserResponseDTO getUserById(Long userId);

    AdminUserResponseDTO getUserByEmail(String email);

    /**
     * Cập nhật name cho người dùng trực tiếp vào onboarding-service qua gRPC
     */
    AdminUserResponseDTO updateUserName(Long userId, UpdateUserNameRequestDTO requestDTO);
}
