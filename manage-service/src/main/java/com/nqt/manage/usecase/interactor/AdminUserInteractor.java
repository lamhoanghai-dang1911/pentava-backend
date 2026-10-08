package com.nqt.manage.usecase.interactor;

import com.nqt.manage.domain.exception.BusinessException;
import com.nqt.manage.domain.exception.ResourceNotFoundException;
import com.nqt.manage.domain.model.AdminUser;
import com.nqt.manage.usecase.boundary.AdminUserInputPort;
import com.nqt.manage.usecase.boundary.AuthServiceGateway;
import com.nqt.manage.usecase.boundary.OnboardingServiceGateway;
import com.nqt.manage.usecase.dto.AdminUserResponseDTO;
import com.nqt.manage.usecase.dto.UpdateUserNameRequestDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserInteractor implements AdminUserInputPort {

    private static final Logger log = LoggerFactory.getLogger(AdminUserInteractor.class);

    private final AuthServiceGateway authServiceGateway;
    private final OnboardingServiceGateway onboardingServiceGateway;

    @Override
    public java.util.List<AdminUserResponseDTO> getAllUsers(String search) {
        log.info("Lấy toàn bộ danh sách người dùng với search='{}'", search);
        java.util.List<AdminUser> users = authServiceGateway.getAllUsers(search);
        if (users == null || users.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        // Lấy danh sách ID để tra cứu tên từ onboarding-service trong 1 lần gọi
        java.util.List<Long> userIds = users.stream()
                .map(AdminUser::getId)
                .filter(java.util.Objects::nonNull)
                .toList();

        java.util.Map<Long, String> profileNames = onboardingServiceGateway.getProfileNamesByUserIds(userIds);

        return users.stream().map(user -> {
            String onboardingName = profileNames.get(user.getId());
            if (onboardingName != null && !onboardingName.isBlank()) {
                user.setName(onboardingName);
            } else if (user.getName() == null) {
                user.setName("");
            }
            return toDTO(user);
        }).toList();
    }

    @Override
    public AdminUserResponseDTO getUserById(Long userId) {
        AdminUser user = authServiceGateway.getUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        // ⚠️ QUAN TRỌNG: Không dùng trường name bên auth-service. Lấy name từ onboarding-service qua gRPC!
        String resolvedName = resolveNameFromOnboarding(user.getId(), user.getEmail());
        user.setName(resolvedName);

        return toDTO(user);
    }

    @Override
    public AdminUserResponseDTO getUserByEmail(String email) {
        AdminUser user = authServiceGateway.getUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với email: " + email));

        // ⚠️ QUAN TRỌNG: Lấy name từ onboarding-service
        String resolvedName = resolveNameFromOnboarding(user.getId(), user.getEmail());
        user.setName(resolvedName);

        return toDTO(user);
    }

    @Override
    public AdminUserResponseDTO updateUserName(Long userId, UpdateUserNameRequestDTO requestDTO) {
        AdminUser user = authServiceGateway.getUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        String newName = requestDTO.getName().trim();
        log.info("Cập nhật tên người dùng userId={} sang '{}' trong onboarding-service", userId, newName);

        // ⚠️ Cập nhật tên trực tiếp sang onboarding-service qua gRPC
        boolean success = onboardingServiceGateway.updateProfileName(userId, user.getEmail(), newName);
        if (!success) {
            throw new BusinessException("Không thể cập nhật tên người dùng tại onboarding-service");
        }

        user.setName(newName);
        return toDTO(user);
    }

    /**
     * Tra cứu tên từ onboarding-service theo userId và email
     */
    private String resolveNameFromOnboarding(Long userId, String email) {
        return onboardingServiceGateway.getProfileNameByUserId(userId)
                .or(() -> onboardingServiceGateway.getProfileNameByEmail(email))
                .filter(name -> !name.isBlank())
                .orElse("");
    }

    private AdminUserResponseDTO toDTO(AdminUser user) {
        return AdminUserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName()) // name đã được đồng bộ từ onboarding-service
                .role(user.getRole())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .isVerified(user.getIsVerified())
                .hasCompletedOnboarding(user.getHasCompletedOnboarding())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
