package com.nqt.onboarding.usecase.interactor;

import com.nqt.onboarding.infrastructure.persistence.TaskProgressEntity;
import com.nqt.onboarding.usecase.boundary.TaskProgressInputPort;
import com.nqt.onboarding.usecase.boundary.TaskProgressOutputPort;
import com.nqt.onboarding.usecase.boundary.TaskProgressRepositoryGateway;
import com.nqt.onboarding.usecase.dto.TaskProgressResponseDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Transactional
@RequiredArgsConstructor
@Service
public class TaskProgressInteractor implements TaskProgressInputPort {

    private final TaskProgressRepositoryGateway repositoryGateway;
    private final TaskProgressOutputPort outputPort;

    @Override
    public void getTaskProgress(Long taskId) {
        Long userId = 1L; // Hoặc lấy từ Token / Security Context
        LocalDate today = LocalDate.now();

        TaskProgressEntity entity = repositoryGateway.findByUserAndTaskAndDate(userId, taskId, today)
                .orElseGet(() -> TaskProgressEntity.builder()
                        .userId(userId)
                        .taskId(taskId)
                        .taskDate(today)
                        .currentCount(0)
                        .targetCount(5) // Mục tiêu mặc định là 5 khẩu phần
                        .isCompleted(false)
                        .build());

        outputPort.present(TaskProgressResponseDTO.builder()
                .message("Lấy tiến độ task thành công")
                .taskId(taskId)
                .currentCount(entity.getCurrentCount())
                .targetCount(entity.getTargetCount())
                .isCompleted(entity.getIsCompleted())
                .taskDate(entity.getTaskDate())
                .build());
    }

    @Override
    public void incrementTaskProgress(Long taskId) {
        Long userId = 1L;
        LocalDate today = LocalDate.now();

        TaskProgressEntity entity = repositoryGateway.findByUserAndTaskAndDate(userId, taskId, today)
                .orElseGet(() -> TaskProgressEntity.builder()
                        .userId(userId)
                        .taskId(taskId)
                        .taskDate(today)
                        .currentCount(0)
                        .targetCount(5)
                        .isCompleted(false)
                        .build());

        // Nếu chưa hoàn thành thì tăng tiến độ lên
        if (!entity.getIsCompleted()) {
            int newCount = entity.getCurrentCount() + 1;
            if (newCount >= entity.getTargetCount()) {
                newCount = entity.getTargetCount();
                entity.setIsCompleted(true); // Đạt mốc tối đa -> Tự động hoàn thành
            }
            entity.setCurrentCount(newCount);
            repositoryGateway.save(entity);
        }

        outputPort.present(TaskProgressResponseDTO.builder()
                .message("Cập nhật tiến độ thành công")
                .taskId(taskId)
                .currentCount(entity.getCurrentCount())
                .targetCount(entity.getTargetCount())
                .isCompleted(entity.getIsCompleted())
                .taskDate(entity.getTaskDate())
                .build());
    }
}