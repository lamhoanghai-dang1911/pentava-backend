package com.nqt.onboarding.usecase.interactor;

import com.nqt.onboarding.domain.*;
import com.nqt.onboarding.infrastructure.groq.GroqDiagnosticService;
import com.nqt.onboarding.infrastructure.persistence.DailyMoodSelectionEntity;
import com.nqt.onboarding.infrastructure.persistence.MoodType;
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

@RequiredArgsConstructor
@Service
public class OnboardingInteractor implements OnboardingInputPort {

    private static final Logger logger = LoggerFactory.getLogger(OnboardingInteractor.class);
    private static final int TASKS_PER_MOOD = 20;
    private static final int TASKS_PER_WEEK_PER_MOOD = 5; // 20 / 4 tuần
    private static final int REQUIRED_DAILY_TASKS = 5;

    private final UserProfileRepositoryGateway profileRepository;
    private final TaskRepositoryGateway taskRepository;
    private final UserGoalRepositoryGateway goalRepository;
    private final OnboardingOutputPort outputPort;
    private final UserServiceGateway userServiceGateway;
    private final CurrentUserProvider currentUserProvider;
    private final GroqDiagnosticService groqDiagnosticService;
    private final TaskProgressRepositoryGateway taskProgressRepository;
    private final DailyMoodSelectionRepositoryGateway moodSelectionRepository;

    // ==========================================
    // ONBOARDING LẦN ĐẦU
    // ==========================================
    @Override
    public void submitOnboarding(OnboardingRequestDTO dto) {
        String email = currentUserProvider.getCurrentUserEmail();
        Long userId = userServiceGateway.getUserIdByEmail(email)
                .orElseThrow(() -> new BusinessException("User không tồn tại trong auth-service: " + email));

        logger.info("Processing onboarding for user: {}", email);

        // Idempotency: chặn nếu đang có goal IN_PROGRESS
        goalRepository.findByEmailAndGoalName(email, dto.getPrimaryGoal())
                .filter(g -> g.getStatus() == GoalStatus.IN_PROGRESS)
                .ifPresent(g -> {
                    throw new BusinessException("Bạn đang có mục tiêu đang thực hiện, không thể tạo mới");
                });

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

        // Gọi Groq NGOÀI transaction DB (method này không @Transactional)
        logger.info("Calling Groq for diagnostic analysis...");
        DiagnosticReportDTO diagnostic = groqDiagnosticService.analyzeDiagnostic(answers);
        logger.info("Groq analysis complete. Overall score: {}", diagnostic.getOverallScore());

        // Phần lưu DB tách thành transaction riêng, ngắn gọn
        Long goalId = persistOnboardingResult(userId, email, dto, diagnostic);

        userServiceGateway.updateOnboardingStatus(userId, true);
        logger.info("Đã cập nhật hasCompletedOnboarding = true cho userId: {}", userId);

        outputPort.present(OnboardingResponseDTO.builder()
                .message("Lưu thông tin khảo sát thành công")
                .diagnostic(diagnostic)
                .goalId(goalId)
                .build());
    }

    @Transactional
    protected Long persistOnboardingResult(Long userId, String email, OnboardingRequestDTO dto, DiagnosticReportDTO diagnostic) {
        String currentGoalName = dto.getPrimaryGoal();

        // Tạo goal mới trước để lấy goalId gắn cho 60 task
        UserGoal primaryGoal = UserGoal.builder()
                .userId(userId)
                .userEmail(email)
                .goalName(currentGoalName)
                .status(GoalStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .build();
        primaryGoal = goalRepository.save(primaryGoal);
        Long goalId = primaryGoal.getId();

        List<TaskEntity> tasksToSave = buildMoodTasks(userId, goalId, currentGoalName, diagnostic);
        taskRepository.saveAll(tasksToSave);
        logger.info("Đã lưu {} task (60 kỳ vọng) cho user: {}, goalId: {}", tasksToSave.size(), email, goalId);

        String freeTimesStr = dto.getFreeTimes() != null ? String.join(",", dto.getFreeTimes()) : "";

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

        return goalId;
    }

    /**
     * Sinh 60 TaskEntity (20 BAD + 20 NEUTRAL + 20 GOOD) từ diagnostic.moodTaskPlan.
     * Nếu Groq trả thiếu, ném lỗi ngay (đã validate ở GroqDiagnosticService, đây là lớp bảo vệ thứ 2).
     */
    private List<TaskEntity> buildMoodTasks(Long userId, Long goalId, String goalName, DiagnosticReportDTO diagnostic) {
        List<TaskEntity> tasksToSave = new ArrayList<>();
        DiagnosticReportDTO.MoodTaskPlanDTO plan = diagnostic.getMoodTaskPlan();

        if (plan == null) {
            throw new BusinessException("Không thể tạo nhiệm vụ: dữ liệu từ AI không hợp lệ");
        }

        addTasksForMood(tasksToSave, userId, goalId, goalName, MoodType.BAD, plan.getBadTasks());
        addTasksForMood(tasksToSave, userId, goalId, goalName, MoodType.NEUTRAL, plan.getNeutralTasks());
        addTasksForMood(tasksToSave, userId, goalId, goalName, MoodType.GOOD, plan.getGoodTasks());

        return tasksToSave;
    }

    private void addTasksForMood(List<TaskEntity> sink, Long userId, Long goalId, String goalName,
                                 MoodType mood, List<String> actions) {
        if (actions == null || actions.size() != TASKS_PER_MOOD) {
            throw new BusinessException(String.format(
                    "Số nhiệm vụ cho mood %s không hợp lệ: %d (yêu cầu %d)",
                    mood, actions == null ? 0 : actions.size(), TASKS_PER_MOOD));
        }

        for (int i = 0; i < actions.size(); i++) {
            int weekNum = Math.min((i / TASKS_PER_WEEK_PER_MOOD) + 1, 4);
            sink.add(TaskEntity.builder()
                    .userId(userId)
                    .goalId(goalId)
                    .goalName(goalName)
                    .moodType(mood)
                    .title(String.format("Nhiệm vụ %s-%02d", mood.name(), i + 1))
                    .content(actions.get(i))
                    .weekNumber(weekNum)
                    .isAiGenerated(true)
                    .isCompleted(false)
                    .build());
        }
    }

    // ==========================================
    // KHẢO SÁT LẠI SAU 4 TUẦN (renewal)
    // ==========================================
    @Override
    public void submitGoalRenewal(GoalRenewalRequestDTO dto) {
        String email = currentUserProvider.getCurrentUserEmail();
        Long userId = userServiceGateway.getUserIdByEmail(email)
                .orElseThrow(() -> new BusinessException("User không tồn tại: " + email));

        // Chặn double-submit: không cho renewal nếu đã có goal IN_PROGRESS
        goalRepository.findByEmailAndGoalName(email, dto.getPrimaryGoal())
                .filter(g -> g.getStatus() == GoalStatus.IN_PROGRESS)
                .ifPresent(g -> {
                    throw new BusinessException("Bạn đang có mục tiêu đang thực hiện, không thể tạo mới");
                });

        OnboardingAnswerDTO answers = OnboardingAnswerDTO.builder()
                .userEmail(email)
                .primaryGoal(dto.getPrimaryGoal())
                .routineType(dto.getRoutineType())
                .sleepHours(dto.getSleepHours())
                .exerciseFrequency(dto.getExerciseFrequency())
                .stressFrequency(dto.getStressFrequency())
                .screenTime(dto.getScreenTime())
                .habitDuration(dto.getHabitDuration())
                .freeTimes(dto.getFreeTimes())
                .build();

        logger.info("Calling Groq for goal renewal analysis...");
        DiagnosticReportDTO diagnostic = groqDiagnosticService.analyzeDiagnostic(answers);

        Long goalId = persistGoalRenewal(userId, email, dto, diagnostic);

        outputPort.present(OnboardingResponseDTO.builder()
                .message("Đã tạo mục tiêu mới thành công")
                .diagnostic(diagnostic)
                .goalId(goalId)
                .build());
    }

    @Transactional
    protected Long persistGoalRenewal(Long userId, String email, GoalRenewalRequestDTO dto, DiagnosticReportDTO diagnostic) {
        UserGoal newGoal = UserGoal.builder()
                .userId(userId)
                .userEmail(email)
                .goalName(dto.getPrimaryGoal())
                .status(GoalStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .build();
        newGoal = goalRepository.save(newGoal);
        Long goalId = newGoal.getId();

        List<TaskEntity> tasksToSave = buildMoodTasks(userId, goalId, dto.getPrimaryGoal(), diagnostic);
        taskRepository.saveAll(tasksToSave);
        logger.info("Đã lưu {} task cho renewal, user: {}, goalId: {}", tasksToSave.size(), email, goalId);

        // Cập nhật lại các field routine trong profile (giữ name/age/gender cũ)
        UserProfile profile = profileRepository.findByEmail(email).orElse(null);
        if (profile != null) {
            profile.setRoutine(dto.getRoutineType());
            profile.setSleepHours(dto.getSleepHours());
            profile.setExerciseFrequency(dto.getExerciseFrequency());
            profile.setStressFrequency(dto.getStressFrequency());
            profile.setScreenTime(dto.getScreenTime());
            profile.setHabitDuration(dto.getHabitDuration());
            profile.setFreeTimes(dto.getFreeTimes() != null ? String.join(",", dto.getFreeTimes()) : "");
            profileRepository.save(profile);
        }

        return goalId;
    }

    // ==========================================
    // LUỒNG HẰNG NGÀY: chọn mood -> hiện 20 task
    // ==========================================
    @Override
    @Transactional
    public void selectMoodAndGetTasks(MoodSelectionRequestDTO requestDTO) {
        String email = currentUserProvider.getCurrentUserEmail();
        Long userId = userServiceGateway.getUserIdByEmail(email)
                .orElseThrow(() -> new BusinessException("User không tồn tại: " + email));

        UserGoal goal = requireActiveGoal(userId, requestDTO.getGoalId());

        MoodType mood;
        try {
            mood = MoodType.valueOf(requestDTO.getMoodType());
        } catch (Exception e) {
            throw new BusinessException("Mood không hợp lệ: " + requestDTO.getMoodType());
        }

        LocalDate today = LocalDate.now();

        // Upsert mood selection trong ngày (nếu đổi ý chọn lại mood trong ngày thì ghi đè)
        DailyMoodSelectionEntity moodEntity = moodSelectionRepository
                .findByUserGoalAndDate(userId, goal.getId(), today)
                .orElseGet(() -> DailyMoodSelectionEntity.builder()
                        .userId(userId)
                        .goalId(goal.getId())
                        .selectionDate(today)
                        .build());
        moodEntity.setMoodType(mood);
        moodEntity.setComment(requestDTO.getComment());
        moodSelectionRepository.save(moodEntity);

        List<TaskEntity> pool = taskRepository.findByUserIdAndGoalIdAndMoodType(userId, goal.getId(), mood);

        LocalDate yesterday = today.minusDays(1);
        List<Long> yesterdayTaskIds = taskProgressRepository.findCompletedTaskIdsByUserAndDate(userId, yesterday);

        List<TaskResponseDTO> taskDTOs = pool.stream().map(this::toTaskResponseDTO).toList();

        outputPort.present(OnboardingResponseDTO.builder()
                .message("Lấy danh sách nhiệm vụ theo cảm xúc thành công")
                .moodTaskResponse(MoodTaskResponseDTO.builder()
                        .moodType(mood.name())
                        .availableTasks(taskDTOs)
                        .yesterdayTaskIds(yesterdayTaskIds)
                        .build())
                .build());
    }

    @Override
    @Transactional
    public void confirmDailyTasks(ConfirmDailyTasksRequestDTO requestDTO) {
        String email = currentUserProvider.getCurrentUserEmail();
        Long userId = userServiceGateway.getUserIdByEmail(email)
                .orElseThrow(() -> new BusinessException("User không tồn tại: " + email));

        requireActiveGoal(userId, requestDTO.getGoalId());

        List<Long> selectedTaskIds = requestDTO.getSelectedTaskIds();
        if (selectedTaskIds == null || selectedTaskIds.size() != REQUIRED_DAILY_TASKS) {
            throw new BusinessException("Phải chọn đúng " + REQUIRED_DAILY_TASKS + " nhiệm vụ");
        }

        LocalDate today = LocalDate.now();

        List<TaskProgressEntity> entries = selectedTaskIds.stream()
                .map(taskId -> taskProgressRepository.findByUserTaskAndDate(userId, taskId, today)
                        .orElseGet(() -> TaskProgressEntity.builder()
                                .userId(userId)
                                .taskId(taskId)
                                .taskDate(today)
                                .isCompleted(false)
                                .build()))
                .toList();

        entries.forEach(taskProgressRepository::save);

        logger.info("User {} đã chọn {} nhiệm vụ cho ngày {}", email, selectedTaskIds.size(), today);

        outputPort.present(OnboardingResponseDTO.builder()
                .message("Đã xác nhận nhiệm vụ hôm nay")
                .build());
    }

    private UserGoal requireActiveGoal(Long userId, Long goalId) {
        // Lưu ý: UserGoalRepositoryGateway hiện chỉ có findByEmailAndGoalName.
        // Cần thêm method findById hoặc findByIdAndUserId để check chuẩn theo goalId + userId.
        UserGoal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy mục tiêu"));

        if (!goal.getUserId().equals(userId)) {
            throw new BusinessException("Mục tiêu không thuộc về user này");
        }
        if (goal.getStatus() != GoalStatus.IN_PROGRESS) {
            throw new BusinessException("Mục tiêu này không còn hoạt động");
        }
        if (goal.isExpired()) {
            goal.setStatus(GoalStatus.COMPLETED);
            goal.setCompletedAt(LocalDateTime.now());
            goalRepository.save(goal);
            throw new BusinessException("Mục tiêu đã hết hạn 4 tuần, vui lòng làm khảo sát lại");
        }
        return goal;
    }

    private TaskResponseDTO toTaskResponseDTO(TaskEntity entity) {
        return TaskResponseDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .weekNumber(entity.getWeekNumber())
                .isAiGenerated(entity.getIsAiGenerated())
                .isCompleted(entity.getIsCompleted())
                .moodType(entity.getMoodType() != null ? entity.getMoodType().name() : null)
                .build();
    }

    // ==========================================
    // CÁC METHOD CŨ GIỮ NGUYÊN LOGIC (đã bỏ @Transactional class-level, có transaction riêng)
    // ==========================================

    @Override
    @Transactional
    public void progressToNextGoal(GoalProgressionRequestDTO requestDTO) {
        String email = currentUserProvider.getCurrentUserEmail();
        Long userId = userServiceGateway.getUserIdByEmail(email)
                .orElseThrow(() -> new BusinessException("User không tồn tại trong auth-service: " + email));

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

        outputPort.present(OnboardingResponseDTO.builder()
                .message("Đã mở khóa mục tiêu tiếp theo")
                .build());
    }

    @Override
    @Transactional
    public void getTasksByWeek(Integer weekNumber) {
        String email = currentUserProvider.getCurrentUserEmail();
        Long userId = userServiceGateway.getUserIdByEmail(email)
                .orElseThrow(() -> new BusinessException("User không tồn tại: " + email));

        List<TaskEntity> entities = taskRepository.findTasksForUserAndWeek(userId, weekNumber);
        List<TaskResponseDTO> taskDTOs = entities.stream().map(this::toTaskResponseDTO).toList();

        outputPort.present(OnboardingResponseDTO.builder()
                .message("Lấy danh sách nhiệm vụ thành công")
                .tasks(taskDTOs)
                .build());
    }

    @Override
    @Transactional
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

        outputPort.present(OnboardingResponseDTO.builder()
                .message("Lưu nháp thành công")
                .build());
    }

    @Override
    @Transactional
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

        outputPort.present(OnboardingResponseDTO.builder()
                .message("Lấy thông tin tiến độ task thành công")
                .taskProgress(TaskProgressResponseDTO.builder()
                        .taskId(taskId)
                        .taskDate(today)
                        .isCompleted(isCompleted)
                        .selectedItems(items)
                        .build())
                .build());
    }

    @Override
    @Transactional
    public void completeTaskProgress(Long taskId, TaskProgressRequestDTO requestDTO) {
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

        if (requestDTO != null && requestDTO.getSelectedItems() != null) {
            String itemsStr = !requestDTO.getSelectedItems().isEmpty()
                    ? String.join(",", requestDTO.getSelectedItems())
                    : "";
            progress.setSelectedItems(itemsStr);
        }

        progress.setIsCompleted(true);
        taskProgressRepository.save(progress);

        outputPort.present(OnboardingResponseDTO.builder()
                .message("Chúc mừng! Bạn đã hoàn thành nhiệm vụ")
                .taskProgress(TaskProgressResponseDTO.builder()
                        .taskId(taskId)
                        .taskDate(today)
                        .isCompleted(true)
                        .selectedItems(progress.getSelectedItems() != null
                                ? List.of(progress.getSelectedItems().split(","))
                                : List.of())
                        .build())
                .build());
    }
}