package com.nqt.auth.interfaceadapters.grpc;

import com.nqt.auth.domain.User;
import com.nqt.auth.infrastructure.persistence.UserEntity;
import com.nqt.auth.usecase.boundary.GetUserQueryInputPort;
import com.nqt.auth.usecase.dto.UserQueryResponseDTO;
import com.nqt.auth.usecase.interactor.UserRepositoryGateway;
import com.nqt.common.grpc.user.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.Optional;

import java.util.Optional;

@GrpcService
public class UserGrpcServiceAdapter extends UserServiceGrpc.UserServiceImplBase {

    private final GetUserQueryInputPort getUserQueryInputPort;
    private final UserRepositoryGateway userRepositoryGateway;
    private final com.nqt.auth.interfaceadapters.gateway.UserJpaRepository userJpaRepository;

    public UserGrpcServiceAdapter(GetUserQueryInputPort getUserQueryInputPort,
                                  UserRepositoryGateway userRepositoryGateway,
                                  com.nqt.auth.interfaceadapters.gateway.UserJpaRepository userJpaRepository) {
        this.getUserQueryInputPort = getUserQueryInputPort;
        this.userRepositoryGateway = userRepositoryGateway;
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public void getUserByEmail(GetUserByEmailRequest request, StreamObserver<UserResponse> responseObserver) {
        Optional<UserQueryResponseDTO> userOpt = getUserQueryInputPort.getUserByEmail(request.getEmail());
        responseObserver.onNext(toResponse(userOpt));
        responseObserver.onCompleted();
    }

    @Override
    public void getUserId(GetUserIdRequest request, StreamObserver<UserResponse> responseObserver) {
        Optional<UserQueryResponseDTO> userOpt = getUserQueryInputPort.getUserById(request.getUserId());
        responseObserver.onNext(toResponse(userOpt));
        responseObserver.onCompleted();
    }

    // 🚀 Xử lý gRPC update trạng thái onboarding trực tiếp tại đây
    @Override
    public void updateOnboardingStatus(UpdateOnboardingStatusRequest request,
                                       StreamObserver<UpdateOnboardingStatusResponse> responseObserver) {
        try {
            Long userId = request.getUserId();
            boolean status = request.getHasCompletedOnboarding();

            // Tìm user theo ID (hoặc viết thêm hàm findById trong UserRepositoryGateway nếu chưa có)
            Optional<User> userOpt = userRepositoryGateway.findById(userId);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setHasCompletedOnboarding(status);
                userRepositoryGateway.save(user); // Lưu lại xuống database

                UpdateOnboardingStatusResponse response = UpdateOnboardingStatusResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("Cập nhật trạng thái onboarding thành công cho user ID: " + userId)
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                UpdateOnboardingStatusResponse response = UpdateOnboardingStatusResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Không tìm thấy user với id: " + userId)
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        } catch (Exception e) {
            UpdateOnboardingStatusResponse response = UpdateOnboardingStatusResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Lỗi server: " + e.getMessage())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getSuggestedUsers(GetSuggestedUsersRequest request, StreamObserver<GetSuggestedUsersResponse> responseObserver) {
        try {
            int limit = request.getLimit() > 0 ? request.getLimit() : 20;
            java.util.List<Long> excluded = new java.util.ArrayList<>(request.getExcludedUserIdsList());
            if (request.getCurrentUserId() > 0 && !excluded.contains(request.getCurrentUserId())) {
                excluded.add(request.getCurrentUserId());
            }
            if (excluded.isEmpty()) {
                excluded.add(-1L);
            }

            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
            java.util.List<UserEntity> candidates = userJpaRepository.findSuggestedUsers(excluded, pageable);

            GetSuggestedUsersResponse.Builder builder = GetSuggestedUsersResponse.newBuilder();
            for (UserEntity u : candidates) {
                builder.addUsers(UserSocialProfile.newBuilder()
                        .setId(u.getId())
                        .setEmail(u.getEmail() != null ? u.getEmail() : "")
                        .setName(u.getName() != null ? u.getName() : "")
                        .setAvatarUrl(u.getAvatarUrl() != null ? u.getAvatarUrl() : "")
                        .setBio(u.getBio() != null ? u.getBio() : "")
                        .build());
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    private UserResponse toResponse(Optional<UserQueryResponseDTO> userOpt) {
        return userOpt
                .map(u -> UserResponse.newBuilder()
                        .setId(u.getId())
                        .setEmail(u.getEmail())
                        .setName(u.getName() == null ? "" : u.getName())
                        .setAvatarUrl(u.getAvatarUrl() == null ? "" : u.getAvatarUrl())
                        .setBio(u.getBio() == null ? "" : u.getBio())
                        .setFound(true)
                        .build())
                .orElse(UserResponse.newBuilder().setFound(false).build());
    }
}