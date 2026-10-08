package com.nqt.manage.usecase.boundary;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OnboardingServiceGateway {
    /**
     * Lấy tên người dùng từ onboarding-service theo userId
     */
    Optional<String> getProfileNameByUserId(Long userId);

    /**
     * Lấy tên người dùng từ onboarding-service theo email
     */
    Optional<String> getProfileNameByEmail(String email);

    /**
     * Cập nhật tên người dùng trực tiếp trong onboarding-service
     */
    boolean updateProfileName(Long userId, String email, String name);

    /**
     * Lấy map tên người dùng theo danh sách userId từ onboarding-service
     */
    Map<Long, String> getProfileNamesByUserIds(List<Long> userIds);
}
