package com.nqt.checkin.usecase.interactor;

import com.nqt.checkin.domain.CheckinRepositoryGateway;
import com.nqt.checkin.infrastructure.persistence.CheckinEntity;
import com.nqt.checkin.usecase.boundary.CheckinInputPort;
import com.nqt.checkin.usecase.boundary.CheckinOutputPort;
import com.nqt.checkin.usecase.dto.CheckinRequestDTO;
import com.nqt.checkin.usecase.dto.CheckinResponseDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Transactional
@RequiredArgsConstructor
@Service
public class CheckinInteractor implements CheckinInputPort {

    private final CheckinRepositoryGateway checkinRepositoryGateway;
    private final CheckinOutputPort outputPort;

    @Override
    public void saveCheckinImageUrl(Long taskProgressId, CheckinRequestDTO requestDTO) {
        Long userId = resolveCurrentUserId();
        LocalDate today = LocalDate.now();

        CheckinEntity entity = checkinRepositoryGateway.findByUserTaskProgressAndDate(userId, taskProgressId, today)
                .orElseGet(() -> CheckinEntity.builder()
                        .userId(userId)
                        .taskProgressId(taskProgressId)
                        .checkinDate(today)
                        .createdAt(LocalDateTime.now())
                        .build());

        entity.setImageUrl(requestDTO.getImageUrl());
        checkinRepositoryGateway.save(entity);

        outputPort.present(CheckinResponseDTO.builder()
                .message("Lưu đường dẫn ảnh check-in thành công")
                .taskProgressId(taskProgressId)
                .imageUrl(requestDTO.getImageUrl())
                .checkinDate(today)
                .build());
    }

    @Override
    public void getCheckinByTask(Long taskProgressId) {
        Long userId = resolveCurrentUserId();
        LocalDate today = LocalDate.now();

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

        outputPort.present(CheckinResponseDTO.builder()
                .message("Lấy thông tin check-in thành công")
                .taskProgressId(taskProgressId)
                .imageUrl(entity.getImageUrl())
                .checkinDate(entity.getCheckinDate())
                .build());
    }

    private Long resolveCurrentUserId() {
        // TODO: hardcode tạm 1L — cần inject CurrentUserProvider + UserServiceGateway
        // giống bên onboarding-service để lấy đúng userId theo JWT của từng user.
        return 1L;
    }
}