package com.nqt.onboarding.usecase.interactor;

import com.nqt.onboarding.domain.*;
import com.nqt.onboarding.infrastructure.groq.GroqDiagnosticService;
import com.nqt.onboarding.infrastructure.persistence.TaskEntity;
import com.nqt.onboarding.infrastructure.persistence.TaskProgressEntity;
import com.nqt.onboarding.infrastructure.security.CurrentUserProvider;
import com.nqt.onboarding.usecase.boundary.*;
import com.nqt.onboarding.usecase.dto.*;
import com.nqt.onboarding.usecase.exception.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Transactional
@RequiredArgsConstructor
@Service
public class OnboardingInteractor implements OnboardingInputPort {

    private static final Logger logger = LoggerFactory.getLogger(OnboardingInteractor.class);

    private final UserProfileRepositoryGateway profileRepository;
    private final TaskRepositoryGateway taskRepository;
    private final UserGoalRepositoryGateway goalRepository;
    private final OnboardingOutputPort outputPort;
    private final UserServiceGateway userServiceGateway;
    private final CurrentUserProvider currentUserProvider;
    private final GroqDiagnosticService groqDiagnosticService;
    private final TaskProgressRepositoryGateway taskProgressRepository;

    @Override
    public void submitOnboarding(OnboardingRequestDTO dto) {
        // Lấy email từ JWT + resolve userId qua gRPC
        String email = currentUserProvider.getCurrentUserEmail();
        Long userId = userServiceGateway.getUserIdByEmail(email)
                .orElseThrow(() -> new BusinessException("User không tồn tại trong auth-service: " + email));

        logger.info("Processing onboarding for user: {}", email);
        System.out.println("DEBUG gRPC - Email: " + email + " -> Resolved userId: " + userId);

        // Build OnboardingAnswerDTO từ request
        OnboardingAnswerDTO answers = OnboardingAnswerDTO.builder()
                .userEmail(email)
                .name(dto.getName())
                .age(dto.getAge())
                .gender(dto.getGender())
                .primaryGoal(dto.getPrimaryGoal())
                .routineType(dto.getRoutineType())
                .sleepHours(dto.getSleepHours())
                .exerciseFrequency(dto.getExerciseFrequency())
                .stressFrequency(dto.getStressFrequency())
                .screenTime(dto.getScreenTime())
                .habitDuration(dto.getHabitDuration())
                .freeTimes(dto.getFreeTimes())
                .build();

        // Gọi Groq để phân tích diagnostic
        logger.info("Calling Groq for diagnostic analysis...");
        DiagnosticReportDTO diagnostic = groqDiagnosticService.analyzeDiagnostic(answers);
        logger.info("Groq analysis complete. Overall score: {}", diagnostic.getOverallScore());

        // ==========================================
        // 🚀 LƯU TASK CỐ ĐỊNH & 8 TASK AI GEN CHO PRIMARY GOAL
        // ==========================================
        List<TaskEntity> tasksToSave = new ArrayList<>();
        String currentGoalName = dto.getPrimaryGoal();

        // 1. Lưu 3 task cố định hàng ngày
        tasksToSave.add(TaskEntity.builder()
                .userId(userId)
                .goalName(currentGoalName)
                .title("Nhiệm vụ 01")
                .content("Hôm nay tránh ăn thực phẩm siêu chế biến (đồ ăn nhanh, nước ngọt, bánh kẹo)")
                .weekNumber(null)
                .isAiGenerated(false)
                .isCompleted(false)
                .build());

        tasksToSave.add(TaskEntity.builder()
                .userId(userId)
                .goalName(currentGoalName)
                .title("Nhiệm vụ 02")
                .content("Ăn đủ 5 khẩu phần rau củ quả trong ngày hôm nay")
                .weekNumber(null)
                .isAiGenerated(false)
                .isCompleted(false)
                .build());

        tasksToSave.add(TaskEntity.builder()
                .userId(userId)
                .goalName(currentGoalName)
                .title("Nhiệm vụ 03")
                .content("Vận động vừa - nặng ít nhất 30 phút hôm nay")
                .weekNumber(null)
                .isAiGenerated(false)
                .isCompleted(false)
                .build());

        // 2. Trích xuất đúng 8 task AI gen từ action plan duy nhất (2 task / tuần cho 4 tuần)
        if (diagnostic.getActionPlans() != null && !diagnostic.getActionPlans().isEmpty()) {
            DiagnosticReportDTO.ActionPlanDTO primaryPlan = diagnostic.getActionPlans().get(0);
            int taskIndex = 4; // Bắt đầu từ Nhiệm vụ 04 đến 11

            if (primaryPlan.getActions() != null) {
                List<String> actions = primaryPlan.getActions();
                for (int i = 0; i < actions.size(); i++) {
                    String actionText = actions.get(i);
                    String formattedTitle = String.format("Nhiệm vụ %02d", taskIndex);

                    // Tự động phân bổ: cứ mỗi 2 task tăng lên 1 tuần (Tuần 1, 2, 3, 4)
                    int weekNum = (i / 2) + 1;
                    if (weekNum > 4) weekNum = 4;

                    tasksToSave.add(TaskEntity.builder()
                            .userId(userId)
                            .goalName(currentGoalName)
                            .title(formattedTitle)
                            .content(actionText)
                            .weekNumber(weekNum)
                            .isAiGenerated(true)
                            .isCompleted(false)
                            .build());

                    taskIndex++;
                }
            }
        }

        taskRepository.saveAll(tasksToSave);
        logger.info("Successfully saved {} tasks to database for user: {}", tasksToSave.size(), email);
        // ==========================================

        String freeTimesStr = dto.getFreeTimes() != null ? String.join(",", dto.getFreeTimes()) : "";

        // Lấy profile cũ hoặc khởi tạo mới
        UserProfile profile = profileRepository.findByEmail(email).orElse(null);
        if (profile == null) {
            profile = UserProfile.builder()
                    .userEmail(email)
                    .userId(userId)
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        profile.setUserId(userId);
        profile.setName(dto.getName());
        profile.setGender(dto.getGender());
        profile.setAge(dto.getAge());
        profile.setRoutine(dto.getRoutineType());
        profile.setSleepHours(dto.getSleepHours());
        profile.setExerciseFrequency(dto.getExerciseFrequency());
        profile.setStressFrequency(dto.getStressFrequency());
        profile.setScreenTime(dto.getScreenTime());
        profile.setHabitDuration(dto.getHabitDuration());
        profile.setFreeTimes(freeTimesStr);

        profileRepository.save(profile);
        logger.info("UserProfile saved for user: {}", email);

        // Chỉ lưu duy nhất primary goal (Bỏ hoàn toàn recommended goals)
        if (dto.getPrimaryGoal() != null && !dto.getPrimaryGoal().isEmpty()) {
            Optional<UserGoal> existingPrimaryGoal = goalRepository.findByEmailAndGoalName(email, dto.getPrimaryGoal());

            if (existingPrimaryGoal.isEmpty()) {
                UserGoal primaryGoal = UserGoal.builder()
                        .userId(userId)
                        .userEmail(email)
                        .goalName(dto.getPrimaryGoal())
                        .status(GoalStatus.IN_PROGRESS)
                        .createdAt(LocalDateTime.now())
                        .build();
                goalRepository.save(primaryGoal);
            } else {
                UserGoal goal = existingPrimaryGoal.get();
                goal.setUserId(userId);
                goalRepository.save(goal);
            }
        }

        // Return response gọn gàng
        OnboardingResponseDTO response = OnboardingResponseDTO.builder()
                .message("Lưu thông tin khảo sát thành công")
                .diagnostic(diagnostic)
                .build();

        outputPort.present(response);
    }

    @Override
    public void progressToNextGoal(GoalProgressionRequestDTO requestDTO) {
        String email = currentUserProvider.getCurrentUserEmail();
        Long userId = userServiceGateway.getUserIdByEmail(email)
                .orElseThrow(() -> new BusinessException("User không tồn tại trong auth-service: " + email));

        logger.info("Progressing goal for user: {}", email);

        UserGoal currentGoal = goalRepository.findByEmailAndGoalName(email, requestDTO.getCurrentGoalName())
                .orElseThrow(() -> new BusinessException("Không tìm thấy mục tiêu hiện tại"));

        currentGoal.setUserId(userId);

        if (currentGoal.getStatus() != GoalStatus.COMPLETED) {
            outputPort.presentError("Bạn phải hoàn thành mục tiêu này trước khi chuyển sang bước tiếp theo");
            return;
        }

        UserGoal nextGoal = goalRepository.findByEmailAndGoalName(email, requestDTO.getNextGoalName())
                .orElseGet(() -> UserGoal.builder()
                        .userId(userId)
                        .userEmail(email)
                        .goalName(requestDTO.getNextGoalName())
                        .createdAt(LocalDateTime.now())
                        .build());

        nextGoal.setUserId(userId);
        nextGoal.setStatus(GoalStatus.IN_PROGRESS);
        goalRepository.save(nextGoal);

        logger.info("Progressed to next goal: {}", requestDTO.getNextGoalName());

        outputPort.present(OnboardingResponseDTO.builder()
                .message("Đã mở khóa mục tiêu tiếp theo")
                .build());
    }

    @Override
    public void getTasksByWeek(Integer weekNumber) {
        String email = currentUserProvider.getCurrentUserEmail();
        Long userId = userServiceGateway.getUserIdByEmail(email)
                .orElseThrow(() -> new BusinessException("User không tồn tại: " + email));

        logger.info("Fetching tasks for user: {} and week: {}", email, weekNumber);

        // Lấy danh sách task từ DB thông qua Gateway
        List<TaskEntity> entities = taskRepository.findTasksForUserAndWeek(userId, weekNumber);

        // Map sang DTO
        List<TaskResponseDTO> taskDTOs = entities.stream().map(entity -> TaskResponseDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .weekNumber(entity.getWeekNumber())
                .isAiGenerated(entity.getIsAiGenerated())
                .isCompleted(entity.getIsCompleted())
                .build()).toList();

        // Đóng gói kết quả gửi về qua Presenter
        outputPort.present(OnboardingResponseDTO.builder()
                .message("Lấy danh sách nhiệm vụ thành công")
                .tasks(taskDTOs)
                .build());
    }

    @Override
    public void saveTaskProgressItems(Long taskId, TaskProgressRequestDTO requestDTO) {
        String email = currentUserProvider.getCurrentUserEmail();
        Long userId = userServiceGateway.getUserIdByEmail(email)
                .orElseThrow(() -> new BusinessException("User không tồn tại: " + email));

        LocalDate today = LocalDate.now();

        TaskProgressEntity progress = taskProgressRepository.findByUserTaskAndDate(userId, taskId, today)
                .orElseGet(() -> TaskProgressEntity.builder()
                        .userId(userId)
                        .taskId(taskId)
                        .taskDate(today)
                        .isCompleted(false)
                        .build());

        String itemsStr = (requestDTO.getSelectedItems() != null && !requestDTO.getSelectedItems().isEmpty())
                ? String.join(",", requestDTO.getSelectedItems())
                : "";

        progress.setSelectedItems(itemsStr);
        taskProgressRepository.save(progress);

        logger.info("Auto-saved task progress items for user: {}, taskId: {}", userId, taskId);

        // Trả về thông báo thành công qua Presenter
        outputPort.present(OnboardingResponseDTO.builder()
                .message("Lưu nháp thành công")
                .build());
    }

    // 2. Lấy lại trạng thái tiến độ đã chọn khi mở app
    @Override
    public void getTaskProgress(Long taskId) {
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

        TaskProgressResponseDTO progressDTO = TaskProgressResponseDTO.builder()
                .taskId(taskId)
                .taskDate(today)
                .isCompleted(isCompleted)
                .selectedItems(items)
                .build();

        // Đóng gói trả về qua Presenter
        outputPort.present(OnboardingResponseDTO.builder()
                .message("Lấy thông tin tiến độ task thành công")
                .taskProgress(progressDTO)
                .build());
    }

    @Override
    public void completeTaskProgress(Long taskId, TaskProgressRequestDTO requestDTO) {
        String email = currentUserProvider.getCurrentUserEmail();
        Long userId = userServiceGateway.getUserIdByEmail(email)
                .orElseThrow(() -> new BusinessException("User không tồn tại: " + email));

        LocalDate today = LocalDate.now();

        // Tìm bản ghi progress trong ngày, nếu chưa có thì tạo mới
        TaskProgressEntity progress = taskProgressRepository.findByUserTaskAndDate(userId, taskId, today)
                .orElseGet(() -> TaskProgressEntity.builder()
                        .userId(userId)
                        .taskId(taskId)
                        .taskDate(today)
                        .isCompleted(false)
                        .build());

        // Lưu các món user đã chọn (nếu có gửi lên cùng lúc)
        if (requestDTO != null && requestDTO.getSelectedItems() != null) {
            String itemsStr = !requestDTO.getSelectedItems().isEmpty()
                    ? String.join(",", requestDTO.getSelectedItems())
                    : "";
            progress.setSelectedItems(itemsStr);
        }

        // Chốt trạng thái hoàn thành thành true
        progress.setIsCompleted(true);
        taskProgressRepository.save(progress);

        logger.info("Task completed for user: {}, taskId: {}", userId, taskId);

        // Trả về response qua Presenter để hiển thị màn hình chúc mừng (Figma: task1-chúc mừng)
        outputPort.present(OnboardingResponseDTO.builder()
                .message("Chúc mừng! Bạn đã hoàn thành nhiệm vụ")
                .taskProgress(TaskProgressResponseDTO.builder()
                        .taskId(taskId)
                        .taskDate(today)
                        .isCompleted(true)
                        .selectedItems(progress.getSelectedItems() != null ? List.of(progress.getSelectedItems().split(",")) : List.of())
                        .build())
                .build());
    }
}