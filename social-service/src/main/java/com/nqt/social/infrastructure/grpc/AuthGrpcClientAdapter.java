package com.nqt.social.infrastructure.grpc;

import com.nqt.common.grpc.user.GetUserByEmailRequest;
import com.nqt.common.grpc.user.GetUserIdRequest;
import com.nqt.common.grpc.user.UserResponse;
import com.nqt.common.grpc.user.UserServiceGrpc;
import com.nqt.social.usecase.boundary.AuthServiceGateway;
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
    public Optional<UserSocialInfo> getUserById(Long userId) {
        try {
            UserResponse response = userServiceStub
                    .withDeadlineAfter(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .getUserId(GetUserIdRequest.newBuilder().setUserId(userId).build());

            if (!response.getFound()) {
                log.warn("User không tìm thấy tại auth-service với ID: {}", userId);
                return Optional.empty();
            }

            return Optional.of(new UserSocialInfo(
                    response.getId(),
                    response.getEmail(),
                    response.getName(),
                    response.getAvatarUrl(),
                    response.getBio()
            ));

        } catch (Exception e) {
            log.error("gRPC call getUserId thất bại với userId: {}", userId, e);
            return Optional.empty();
        }
    }

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
    public java.util.List<UserSocialInfo> getSuggestedUsers(Long currentUserId, java.util.List<Long> excludedUserIds, int limit) {
        try {
            com.nqt.common.grpc.user.GetSuggestedUsersRequest.Builder reqBuilder =
                    com.nqt.common.grpc.user.GetSuggestedUsersRequest.newBuilder()
                            .setCurrentUserId(currentUserId != null ? currentUserId : 0L)
                            .setLimit(limit);

            if (excludedUserIds != null) {
                reqBuilder.addAllExcludedUserIds(excludedUserIds);
            }

            com.nqt.common.grpc.user.GetSuggestedUsersResponse response = userServiceStub
                    .withDeadlineAfter(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .getSuggestedUsers(reqBuilder.build());

            return response.getUsersList().stream()
                    .map(u -> new UserSocialInfo(
                            u.getId(),
                            u.getEmail(),
                            u.getName(),
                            u.getAvatarUrl(),
                            u.getBio()
                    ))
                    .toList();

        } catch (Exception e) {
            log.error("gRPC call getSuggestedUsers thất bại: {}", e.getMessage(), e);
            return java.util.Collections.emptyList();
        }
    }
}
