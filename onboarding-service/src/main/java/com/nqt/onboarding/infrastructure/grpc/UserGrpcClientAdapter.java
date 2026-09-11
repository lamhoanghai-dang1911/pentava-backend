package com.nqt.onboarding.infrastructure.grpc;

import com.nqt.common.grpc.user.GetUserByEmailRequest;
import com.nqt.common.grpc.user.GetUserByIdRequest;
import com.nqt.common.grpc.user.UserResponse;
import com.nqt.common.grpc.user.UserServiceGrpc;
import com.nqt.onboarding.usecase.boundary.UserServiceGateway;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserGrpcClientAdapter implements UserServiceGateway {

    private static final Logger logger = LoggerFactory.getLogger(UserGrpcClientAdapter.class);

    @GrpcClient("auth-service")
    private UserServiceGrpc.UserServiceBlockingStub userServiceStub;

    @Override
    public Optional<Long> getUserIdByEmail(String email) {
        try {
            logger.debug("Gọi gRPC GetUserByEmail với email: {}", email);

            UserResponse response = userServiceStub.getUserByEmail(
                    GetUserByEmailRequest.newBuilder()
                            .setEmail(email)
                            .build()
            );

            if (!response.getFound()) {
                logger.warn("User không tìm thấy ở auth-service: {}", email);
                return Optional.empty();
            }

            logger.debug("Lấy userId từ gRPC: {} → {}", email, response.getId());
            return Optional.of(response.getId());

        } catch (Exception e) {
            logger.error("gRPC call getUserByEmail thất bại với email: {}", email, e);
            throw new RuntimeException("Không thể resolve userId từ auth-service", e);
        }
    }

    @Override
    public Optional<String> getUserEmailById(Long userId) {
        try {
            logger.debug("Gọi gRPC GetUserById với userId: {}", userId);

            UserResponse response = userServiceStub.getUserById(
                    GetUserByIdRequest.newBuilder()
                            .setUserId(userId)
                            .build()
            );

            if (!response.getFound()) {
                logger.warn("User không tìm thấy ở auth-service: {}", userId);
                return Optional.empty();
            }

            return Optional.of(response.getEmail());

        } catch (Exception e) {
            logger.error("gRPC call getUserById thất bại với userId: {}", userId, e);
            throw new RuntimeException("Không thể resolve email từ auth-service", e);
        }
    }
}