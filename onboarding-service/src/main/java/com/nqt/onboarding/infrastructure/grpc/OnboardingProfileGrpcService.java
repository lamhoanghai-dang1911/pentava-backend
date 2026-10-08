package com.nqt.onboarding.infrastructure.grpc;

import com.nqt.common.grpc.onboarding.*;
import com.nqt.onboarding.domain.UserProfile;
import com.nqt.onboarding.usecase.boundary.UserProfileRepositoryGateway;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;

@GrpcService
@RequiredArgsConstructor
public class OnboardingProfileGrpcService extends OnboardingProfileServiceGrpc.OnboardingProfileServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(OnboardingProfileGrpcService.class);

    private final UserProfileRepositoryGateway profileRepository;

    @Override
    public void getProfileByEmail(GetProfileByEmailRequest request,
                                  StreamObserver<OnboardingProfileResponse> responseObserver) {
        try {
            logger.debug("Nhận gRPC GetProfileByEmail cho email: {}", request.getEmail());
            Optional<UserProfile> profileOpt = profileRepository.findByEmail(request.getEmail());
            responseObserver.onNext(toResponse(profileOpt));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Lỗi khi xử lý GetProfileByEmail cho email: {}", request.getEmail(), e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getProfileByUserId(GetProfileByUserIdRequest request,
                                   StreamObserver<OnboardingProfileResponse> responseObserver) {
        try {
            logger.debug("Nhận gRPC GetProfileByUserId cho userId: {}", request.getUserId());
            Optional<UserProfile> profileOpt = profileRepository.findByUserId(request.getUserId());
            responseObserver.onNext(toResponse(profileOpt));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Lỗi khi xử lý GetProfileByUserId cho userId: {}", request.getUserId(), e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void updateProfileName(UpdateProfileNameRequest request,
                                  StreamObserver<UpdateProfileNameResponse> responseObserver) {
        try {
            logger.debug("Nhận gRPC UpdateProfileName: email={}, userId={}, name={}",
                    request.getEmail(), request.getUserId(), request.getName());

            String email = request.getEmail();
            long userId = request.getUserId();
            String name = request.getName();

            UserProfile profile = null;
            if (email != null && !email.isBlank()) {
                profile = profileRepository.findByEmail(email).orElse(null);
            }
            if (profile == null && userId > 0) {
                profile = profileRepository.findByUserId(userId).orElse(null);
            }

            if (profile != null) {
                profile.setName(name);
                if (userId > 0 && (profile.getUserId() == null || profile.getUserId() <= 0)) {
                    profile.setUserId(userId);
                }
                if (email != null && !email.isBlank() && (profile.getUserEmail() == null || profile.getUserEmail().isBlank())) {
                    profile.setUserEmail(email);
                }
                profileRepository.save(profile);
                logger.info("Đã cập nhật tên thành công trong UserProfile cho email: {}, userId: {}", email, userId);
            } else {
                profile = UserProfile.builder()
                        .userId(userId > 0 ? userId : null)
                        .userEmail(email != null && !email.isBlank() ? email : "")
                        .name(name)
                        .createdAt(LocalDateTime.now())
                        .build();
                profileRepository.save(profile);
                logger.info("Đã tạo mới UserProfile với tên trong onboarding-service cho email: {}, userId: {}", email, userId);
            }

            responseObserver.onNext(UpdateProfileNameResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Cập nhật tên thành công")
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Lỗi khi xử lý UpdateProfileName: {}", e.getMessage(), e);
            responseObserver.onNext(UpdateProfileNameResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Lỗi: " + e.getMessage())
                    .build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getProfilesByUserIds(GetProfilesByUserIdsRequest request,
                                     StreamObserver<GetProfilesResponse> responseObserver) {
        try {
            logger.debug("Nhận gRPC GetProfilesByUserIds cho {} userIds", request.getUserIdsCount());
            java.util.List<UserProfile> profiles = profileRepository.findByUserIds(request.getUserIdsList());

            GetProfilesResponse.Builder builder = GetProfilesResponse.newBuilder();
            for (UserProfile p : profiles) {
                if (p.getUserId() != null) {
                    builder.addProfiles(UserProfileInfo.newBuilder()
                            .setUserId(p.getUserId())
                            .setEmail(p.getUserEmail() != null ? p.getUserEmail() : "")
                            .setName(p.getName() != null ? p.getName() : "")
                            .build());
                }
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Lỗi khi xử lý GetProfilesByUserIds: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }

    private OnboardingProfileResponse toResponse(Optional<UserProfile> profileOpt) {
        return profileOpt.map(p -> OnboardingProfileResponse.newBuilder()
                .setFound(true)
                .setUserId(p.getUserId() != null ? p.getUserId() : 0L)
                .setEmail(p.getUserEmail() != null ? p.getUserEmail() : "")
                .setName(p.getName() != null ? p.getName() : "")
                .build()
        ).orElseGet(() -> OnboardingProfileResponse.newBuilder()
                .setFound(false)
                .build());
    }
}
