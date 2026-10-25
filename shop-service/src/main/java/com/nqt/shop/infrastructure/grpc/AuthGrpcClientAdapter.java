package com.nqt.shop.infrastructure.grpc;

import com.nqt.common.grpc.user.GetUserByEmailRequest;
import com.nqt.common.grpc.user.GetUserIdRequest;
import com.nqt.common.grpc.user.UserResponse;
import com.nqt.common.grpc.user.UserServiceGrpc;
import com.nqt.shop.usecase.boundary.AuthServiceGateway;
import com.nqt.shop.usecase.dto.UserDetailDTO;
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
    public Optional<Long> getUserIdByEmail(String email) {
        try {
            UserResponse response = userServiceStub
                    .withDeadlineAfter(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .getUserByEmail(GetUserByEmailRequest.newBuilder().setEmail(email).build());

            if (!response.getFound()) {
                log.warn("User không tìm thấy tại auth-service với email: {}", email);
                return Optional.empty();
            }

            return Optional.of(response.getId());
        } catch (Exception e) {
            log.error("gRPC call getUserByEmail thất bại với email: {}", email, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<UserDetailDTO> getUserById(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        try {
            UserResponse response = userServiceStub
                    .withDeadlineAfter(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .getUserId(GetUserIdRequest.newBuilder().setUserId(userId).build());

            if (!response.getFound()) {
                log.warn("User không tìm thấy tại auth-service với userId: {}", userId);
                return Optional.empty();
            }

            return Optional.of(UserDetailDTO.builder()
                    .id(response.getId())
                    .email(response.getEmail())
                    .name(response.getName())
                    .avatarUrl(response.getAvatarUrl())
                    .build());
        } catch (Exception e) {
            log.warn("gRPC call getUserId thất bại với userId {}: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }
}

