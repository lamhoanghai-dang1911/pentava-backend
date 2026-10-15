package com.nqt.checkin.usecase.interactor;

import com.nqt.checkin.domain.CheckinRepositoryGateway;
import com.nqt.checkin.infrastructure.persistence.CheckinEntity;
import com.nqt.checkin.infrastructure.security.CurrentUserProvider;
import com.nqt.checkin.infrastructure.storage.SupabaseImageUrlValidator;
import com.nqt.checkin.usecase.boundary.CheckinInputPort;
import com.nqt.checkin.usecase.boundary.CheckinOutputPort;
import com.nqt.checkin.usecase.boundary.UserServiceGateway;
import com.nqt.checkin.usecase.dto.CheckinRequestDTO;
import com.nqt.checkin.usecase.dto.CheckinResponseDTO;
import com.nqt.checkin.usecase.exception.CheckinBusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Transactional
@RequiredArgsConstructor
@Service
public class CheckinInteractor implements CheckinInputPort {

    // Cùng múi giờ với onboarding-service để ranh giới ngày/tuần khớp nhau
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final CheckinRepositoryGateway checkinRepositoryGateway;
    private final CheckinOutputPort outputPort;
    private final CurrentUserProvider currentUserProvider;
    private final UserServiceGateway userServiceGateway;
    private final SupabaseImageUrlValidator imageUrlValidator;
    private final com.nqt.checkin.infrastructure.storage.SupabaseSignedUrlService supabaseSignedUrlService;

    @Override
    public void saveCheckinImageUrl(Long taskProgressId, CheckinRequestDTO requestDTO) {
        String rawImageUrl = requestDTO.getImageUrl() == null ? null : requestDTO.getImageUrl().trim();
        imageUrlValidator.requireValid(rawImageUrl);

        Long userId = resolveCurrentUserId();
        LocalDate today = today();

        CheckinEntity entity = checkinRepositoryGateway.findByUserTaskProgressAndDate(userId, taskProgressId, today)
                .orElseGet(() -> CheckinEntity.builder()
                        .userId(userId)
                        .taskProgressId(taskProgressId)
                        .checkinDate(today)
                        .createdAt(LocalDateTime.now(VN_ZONE))
                        .build());

        String storagePath = supabaseSignedUrlService.extractStoragePath(rawImageUrl);
        String savedImageUrl = storagePath != null ? storagePath : rawImageUrl;
        entity.setImageUrl(savedImageUrl);
        if (requestDTO.getFileSizeBytes() != null && requestDTO.getFileSizeBytes() > 0) {
            entity.setFileSizeBytes(requestDTO.getFileSizeBytes());
        }
        checkinRepositoryGateway.save(entity);

        String signedUrl = supabaseSignedUrlService.createSignedUrl(savedImageUrl);
        outputPort.present(CheckinResponseDTO.builder()
                .message("Lưu đường dẫn ảnh check-in thành công")
                .taskProgressId(taskProgressId)
                .imageUrl(signedUrl)
                .checkinDate(today)
                .build());
    }

    @Override
    public void getCheckinByTask(Long taskProgressId) {
        Long userId = resolveCurrentUserId();
        LocalDate today = today();

        CheckinEntity entity = checkinRepositoryGateway.findByUserTaskProgressAndDate(userId, taskProgressId, today)
                .orElse(null);

        if (entity == null) {
            outputPort.present(CheckinResponseDTO.builder()
                    .message("Chưa có thông tin check-in trong ngày")
                    .taskProgressId(taskProgressId)
                    .imageUrl(null)
                    .checkinDate(today)
                    .build());
            return;
        }

        String signedUrl = supabaseSignedUrlService.createSignedUrl(entity.getImageUrl());
        outputPort.present(CheckinResponseDTO.builder()
                .message("Lấy thông tin check-in thành công")
                .taskProgressId(taskProgressId)
                .imageUrl(signedUrl)
                .checkinDate(entity.getCheckinDate())
                .build());
    }

    private Long resolveCurrentUserId() {
        String email = currentUserProvider.getCurrentUserEmail();
        return userServiceGateway.getUserIdByEmail(email)
                .orElseThrow(() -> new CheckinBusinessException("User không tồn tại"));
    }

    private LocalDate today() {
        return LocalDate.now(VN_ZONE);
    }
}