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
    public void saveCheckinImageUrl(Long taskId, CheckinRequestDTO requestDTO) {
        Long userId = 1L; // Hoặc lấy từ token JWT / Security Context hiện tại
        LocalDate today = LocalDate.now();

        // Tìm hoặc tạo mới bản ghi check-in trong ngày của task này
        CheckinEntity entity = checkinRepositoryGateway.findByUserTaskAndDate(userId, taskId, today)
                .orElseGet(() -> CheckinEntity.builder()
                        .userId(userId)
                        .taskId(taskId)
                        .checkinDate(today)
                        .createdAt(LocalDateTime.now())
                        .build());

        // Cập nhật URL ảnh từ Supabase do Frontend gửi lên
        entity.setImageUrl(requestDTO.getImageUrl());
        checkinRepositoryGateway.save(entity);

        outputPort.present(CheckinResponseDTO.builder()
                .message("Lưu đường dẫn ảnh check-in thành công")
                .taskId(taskId)
                .imageUrl(requestDTO.getImageUrl())
                .checkinDate(today)
                .build());
    }

    @Override
    public void getCheckinByTask(Long taskId) {
        Long userId = 1L;
        LocalDate today = LocalDate.now();

        CheckinEntity entity = checkinRepositoryGateway.findByUserTaskAndDate(userId, taskId, today)
                .orElse(null);

        if (entity == null) {
            outputPort.present(CheckinResponseDTO.builder()
                    .message("Chưa có thông tin check-in trong ngày")
                    .taskId(taskId)
                    .imageUrl(null)
                    .checkinDate(today)
                    .build());
            return;
        }

        outputPort.present(CheckinResponseDTO.builder()
                .message("Lấy thông tin check-in thành công")
                .taskId(taskId)
                .imageUrl(entity.getImageUrl())
                .checkinDate(entity.getCheckinDate())
                .build());
    }
}