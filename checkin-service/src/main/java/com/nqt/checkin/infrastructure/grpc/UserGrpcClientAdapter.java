package com.nqt.checkin.infrastructure.grpc;

import com.nqt.checkin.usecase.boundary.UserServiceGateway;
import com.nqt.common.grpc.user.GetUserByEmailRequest;
import com.nqt.common.grpc.user.UserResponse;
import com.nqt.common.grpc.user.UserServiceGrpc;
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
            UserResponse response = userServiceStub.getUserByEmail(
                    GetUserByEmailRequest.newBuilder()
                            .setEmail(email)
                            .build()
            );

            if (!response.getFound()) {
                logger.warn("User không tìm thấy ở auth-service: {}", email);
                return Optional.empty();
            }
            return Optional.of(response.getId());

        } catch (Exception e) {
            logger.error("gRPC call getUserByEmail thất bại với email: {}", email, e);
            throw new RuntimeException("Không thể resolve userId từ auth-service", e);
        }
    }
}