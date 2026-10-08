package com.nqt.manage.usecase.boundary;

import com.nqt.manage.usecase.dto.AdminUserResponseDTO;
import com.nqt.manage.usecase.dto.UpdateUserNameRequestDTO;

import java.util.List;

public interface AdminUserInputPort {
    /**
     * Lấy toàn bộ danh sách người dùng kết hợp:
     * - auth-service (id, email, role, avatar, bio, isVerified, hasCompletedOnboarding, createdAt)
     * - onboarding-service (tên hiển thị / profile name)
     */
    List<AdminUserResponseDTO> getAllUsers(String search);

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
