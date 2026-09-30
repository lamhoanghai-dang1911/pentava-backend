package com.nqt.manage.infrastructure.grpc;

import com.nqt.common.grpc.user.GetUserByEmailRequest;
import com.nqt.common.grpc.user.GetUserIdRequest;
import com.nqt.common.grpc.user.UserResponse;
import com.nqt.common.grpc.user.UserServiceGrpc;
import com.nqt.manage.domain.model.AdminUser;
import com.nqt.manage.usecase.boundary.AuthServiceGateway;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class AuthGrpcClientAdapter implements AuthServiceGateway {

    private static final Logger log = LoggerFactory.getLogger(AuthGrpcClientAdapter.class);
    private static final long TIMEOUT_SECONDS = 5;

    @GrpcClient("auth-service")
    private UserServiceGrpc.UserServiceBlockingStub userServiceStub;

    @Override
    public Optional<AdminUser> getUserById(Long userId) {
        if (userId == null || userId <= 0) {
            return Optional.empty();
        }
        try {
            log.debug("Gọi gRPC getUserId sang auth-service với userId: {}", userId);
            UserResponse response = userServiceStub
                    .withDeadlineAfter(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .getUserId(GetUserIdRequest.newBuilder().setUserId(userId).build());

            if (!response.getFound()) {
                log.warn("Không tìm thấy user trong auth-service với userId: {}", userId);
                return Optional.empty();
            }

            return Optional.of(AdminUser.builder()
                    .id(response.getId())
                    .email(response.getEmail())
                    // ⚠️ KHÔNG lấy response.getName() ở đây vì auth-service không lưu tên chính xác.
                    // Tên sẽ do usecase layer gọi sang onboarding-service để lấy.
                    .name("")
                    .avatarUrl(response.getAvatarUrl())
                    .bio(response.getBio())
                    .isVerified(response.getFound())
                    .build());
        } catch (Exception e) {
            log.error("Lỗi khi gọi gRPC getUserId sang auth-service cho userId {}: {}", userId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<AdminUser> getUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        try {
            log.debug("Gọi gRPC getUserByEmail sang auth-service với email: {}", email);
            UserResponse response = userServiceStub
                    .withDeadlineAfter(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .getUserByEmail(GetUserByEmailRequest.newBuilder().setEmail(email).build());

            if (!response.getFound()) {
                log.warn("Không tìm thấy user trong auth-service với email: {}", email);
                return Optional.empty();
            }

            return Optional.of(AdminUser.builder()
                    .id(response.getId())
                    .email(response.getEmail())
                    // ⚠️ KHÔNG lấy response.getName(), name sẽ lấy từ onboarding-service
                    .name("")
                    .avatarUrl(response.getAvatarUrl())
                    .bio(response.getBio())
                    .isVerified(response.getFound())
                    .build());
        } catch (Exception e) {
            log.error("Lỗi khi gọi gRPC getUserByEmail sang auth-service cho email {}: {}", email, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Long> getUserIdByEmail(String email) {
        return getUserByEmail(email).map(AdminUser::getId);
    }
}
