package com.nqt.onboarding.usecase.interactor;

import com.nqt.onboarding.infrastructure.persistence.TaskProgressEntity;
import com.nqt.onboarding.infrastructure.security.CurrentUserProvider;
import com.nqt.onboarding.usecase.boundary.TaskInputPort;
import com.nqt.onboarding.usecase.boundary.TaskProgressRepositoryGateway;
import com.nqt.onboarding.usecase.boundary.UserServiceGateway;
import com.nqt.onboarding.usecase.dto.TaskProgressRequestDTO;
import com.nqt.onboarding.usecase.dto.TaskProgressResponseDTO;
import com.nqt.onboarding.usecase.exception.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Transactional
@RequiredArgsConstructor
@Service
public class TaskInteractor implements TaskInputPort {

    private static final Logger logger = LoggerFactory.getLogger(TaskInteractor.class);

    private final TaskProgressRepositoryGateway taskProgressRepository;
    private final CurrentUserProvider currentUserProvider;
    private final UserServiceGateway userServiceGateway;

    @Override
    public void saveTaskProgressItems(Long taskId, TaskProgressRequestDTO requestDTO) {
        String email = currentUserProvider.getCurrentUserEmail();
        Long userId = userServiceGateway.getUserIdByEmail(email)
                .orElseThrow(() -> new BusinessException("User không tồn tại: " + email));

        LocalDate today = LocalDate.now();

        // Tìm bản ghi tiến độ trong ngày, nếu chưa có thì khởi tạo mới
        TaskProgressEntity progress = taskProgressRepository.findByUserTaskAndDate(userId, taskId, today)
                .orElseGet(() -> TaskProgressEntity.builder()
                        .userId(userId)
                        .taskId(taskId)
                        .taskDate(today)
                        .isCompleted(false)
                        .build());

        // Chuyển List<String> thành chuỗi phân tách bằng dấu phẩy để lưu DB
        String itemsStr = (requestDTO.getSelectedItems() != null && !requestDTO.getSelectedItems().isEmpty())
                ? String.join(",", requestDTO.getSelectedItems())
                : "";

        progress.setSelectedItems(itemsStr);
        taskProgressRepository.save(progress);

        logger.info("Auto-saved task progress items for user: {}, taskId: {}", userId, taskId);
    }

    @Override
    public TaskProgressResponseDTO getTaskProgress(Long taskId) {
        String email = currentUserProvider.getCurrentUserEmail();
        Long userId = userServiceGateway.getUserIdByEmail(email)
                .orElseThrow(() -> new BusinessException("User không tồn tại: " + email));

        LocalDate today = LocalDate.now();

        TaskProgressEntity progress = taskProgressRepository.findByUserTaskAndDate(userId, taskId, today)
                .orElse(null);

        List<String> items = new ArrayList<>();
        Boolean isCompleted = false;

        if (progress != null) {
            isCompleted = progress.getIsCompleted();
            if (progress.getSelectedItems() != null && !progress.getSelectedItems().isEmpty()) {
                items = List.of(progress.getSelectedItems().split(","));
            }
        }

        return TaskProgressResponseDTO.builder()
                .taskId(taskId)
                .taskDate(today)
                .isCompleted(isCompleted)
                .selectedItems(items)
                .build();
    }
}