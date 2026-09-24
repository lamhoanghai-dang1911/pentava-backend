package com.nqt.auth.interfaceadapters.grpc;

import com.nqt.auth.usecase.boundary.OnboardingProfileGateway;
import com.nqt.common.grpc.onboarding.GetProfileByEmailRequest;
import com.nqt.common.grpc.onboarding.GetProfileByUserIdRequest;
import com.nqt.common.grpc.onboarding.OnboardingProfileResponse;
import com.nqt.common.grpc.onboarding.OnboardingProfileServiceGrpc;
import com.nqt.common.grpc.onboarding.UpdateProfileNameRequest;
import com.nqt.common.grpc.onboarding.UpdateProfileNameResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class OnboardingGrpcClientAdapter implements OnboardingProfileGateway {

    private static final Logger logger = LoggerFactory.getLogger(OnboardingGrpcClientAdapter.class);
    private static final long TIMEOUT_SECONDS = 5;

    @GrpcClient("onboarding-service")
    private OnboardingProfileServiceGrpc.OnboardingProfileServiceBlockingStub onboardingProfileStub;

    @Override
    public Optional<String> getProfileNameByEmail(String email) {
        try {
            logger.debug("Gọi gRPC GetProfileByEmail tới onboarding-service với email: {}", email);
            OnboardingProfileResponse response = onboardingProfileStub
                    .withDeadlineAfter(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .getProfileByEmail(GetProfileByEmailRequest.newBuilder().setEmail(email).build());

            if (response.getFound() && response.getName() != null && !response.getName().isBlank()) {
                logger.debug("Lấy tên từ onboarding-service thành công cho email {}: {}", email, response.getName());
                return Optional.of(response.getName());
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.warn("Không thể lấy tên từ onboarding-service theo email {}: {}", email, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> getProfileNameByUserId(Long userId) {
        try {
            logger.debug("Gọi gRPC GetProfileByUserId tới onboarding-service với userId: {}", userId);
            OnboardingProfileResponse response = onboardingProfileStub
                    .withDeadlineAfter(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .getProfileByUserId(GetProfileByUserIdRequest.newBuilder().setUserId(userId).build());

            if (response.getFound() && response.getName() != null && !response.getName().isBlank()) {
                logger.debug("Lấy tên từ onboarding-service thành công cho userId {}: {}", userId, response.getName());
                return Optional.of(response.getName());
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.warn("Không thể lấy tên từ onboarding-service theo userId {}: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean updateProfileName(Long userId, String email, String name) {
        try {
            logger.debug("Gọi gRPC UpdateProfileName tới onboarding-service: userId={}, email={}, name={}", userId, email, name);
            UpdateProfileNameResponse response = onboardingProfileStub
                    .withDeadlineAfter(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .updateProfileName(UpdateProfileNameRequest.newBuilder()
                            .setUserId(userId != null ? userId : 0L)
                            .setEmail(email != null ? email : "")
                            .setName(name != null ? name : "")
                            .build());

            if (response.getSuccess()) {
                logger.debug("Cập nhật tên ở onboarding-service thành công cho email {}: {}", email, name);
                return true;
            } else {
                logger.warn("onboarding-service trả về thất bại khi cập nhật tên: {}", response.getMessage());
                return false;
            }
        } catch (Exception e) {
            logger.warn("Không thể gọi gRPC updateProfileName sang onboarding-service cho email {}: {}", email, e.getMessage());
            return false;
        }
    }
}
