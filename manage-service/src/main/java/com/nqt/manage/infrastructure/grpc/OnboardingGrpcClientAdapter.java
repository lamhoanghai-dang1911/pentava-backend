package com.nqt.manage.infrastructure.grpc;

import com.nqt.common.grpc.onboarding.GetProfileByEmailRequest;
import com.nqt.common.grpc.onboarding.GetProfileByUserIdRequest;
import com.nqt.common.grpc.onboarding.OnboardingProfileResponse;
import com.nqt.common.grpc.onboarding.OnboardingProfileServiceGrpc;
import com.nqt.common.grpc.onboarding.UpdateProfileNameRequest;
import com.nqt.common.grpc.onboarding.UpdateProfileNameResponse;
import com.nqt.manage.usecase.boundary.OnboardingServiceGateway;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class OnboardingGrpcClientAdapter implements OnboardingServiceGateway {

    private static final Logger log = LoggerFactory.getLogger(OnboardingGrpcClientAdapter.class);
    private static final long TIMEOUT_SECONDS = 5;

    @GrpcClient("onboarding-service")
    private OnboardingProfileServiceGrpc.OnboardingProfileServiceBlockingStub onboardingProfileStub;

    @Override
    public Optional<String> getProfileNameByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            return Optional.empty();
        }
        try {
            log.debug("Gọi gRPC GetProfileByUserId sang onboarding-service với userId: {}", userId);
            OnboardingProfileResponse response = onboardingProfileStub
                    .withDeadlineAfter(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .getProfileByUserId(GetProfileByUserIdRequest.newBuilder().setUserId(userId).build());

            if (response.getFound() && response.getName() != null && !response.getName().isBlank()) {
                log.debug("Lấy tên từ onboarding-service thành công cho userId {}: {}", userId, response.getName());
                return Optional.of(response.getName());
            }
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Không thể lấy tên từ onboarding-service cho userId {}: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> getProfileNameByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        try {
            log.debug("Gọi gRPC GetProfileByEmail sang onboarding-service với email: {}", email);
            OnboardingProfileResponse response = onboardingProfileStub
                    .withDeadlineAfter(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .getProfileByEmail(GetProfileByEmailRequest.newBuilder().setEmail(email).build());

            if (response.getFound() && response.getName() != null && !response.getName().isBlank()) {
                log.debug("Lấy tên từ onboarding-service thành công cho email {}: {}", email, response.getName());
                return Optional.of(response.getName());
            }
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Không thể lấy tên từ onboarding-service cho email {}: {}", email, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean updateProfileName(Long userId, String email, String name) {
        try {
            log.debug("Gọi gRPC UpdateProfileName sang onboarding-service: userId={}, email={}, name={}", userId, email, name);
            UpdateProfileNameResponse response = onboardingProfileStub
                    .withDeadlineAfter(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .updateProfileName(UpdateProfileNameRequest.newBuilder()
                            .setUserId(userId != null ? userId : 0L)
                            .setEmail(email != null ? email : "")
                            .setName(name != null ? name : "")
                            .build());

            if (response.getSuccess()) {
                log.info("Cập nhật tên trong onboarding-service thành công: userId={}, name={}", userId, name);
                return true;
            } else {
                log.warn("onboarding-service trả về thất bại khi cập nhật tên: {}", response.getMessage());
                return false;
            }
        } catch (Exception e) {
            log.error("Lỗi khi gọi gRPC updateProfileName sang onboarding-service: {}", e.getMessage(), e);
            return false;
        }
    }
}
