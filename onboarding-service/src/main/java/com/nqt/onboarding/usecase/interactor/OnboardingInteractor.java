package com.nqt.onboarding.usecase.interactor;

import com.nqt.onboarding.domain.*;
import com.nqt.onboarding.infrastructure.groq.GroqDiagnosticService;
import com.nqt.onboarding.infrastructure.persistence.*;
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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class OnboardingInteractor implements OnboardingInputPort {

    private static final Logger logger = LoggerFactory.getLogger(OnboardingInteractor.class);
    private static final int TASKS_PER_MOOD = 20;
    private static final int TASKS_PER_WEEK_PER_MOOD = 5; // 20 / 4 tuần
    private static final int REQUIRED_DAILY_TASKS = 5;
    private static final int GOAL_DURATION_DAYS = 28; // 4 tuần x 7 ngày, endDate = startDate + 27

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    /** Thứ tự hiển thị trong ngày: theo displayOrder (null xếp sau), sau đó theo id của progress. */
    private static final Comparator<TaskProgressEntity> DAILY_ORDER = Comparator
            .comparing(TaskProgressEntity::getDisplayOrder, Comparator.nullsLast(Comparator.<Integer>naturalOrder()))
            .thenComparing(TaskProgressEntity::getId);

    private final UserProfileRepositoryGateway profileRepository;
    private final TaskRepositoryGateway taskRepository;
    private final UserGoalRepositoryGateway goalRepository;
    private final OnboardingOutputPort outputPort;
    private final UserServiceGateway userServiceGateway;
    private final CurrentUserProvider currentUserProvider;
    private final GroqDiagnosticService groqDiagnosticService;
    private final TaskProgressRepositoryGateway taskProgressRepository;
    private final DailyMoodSelectionRepositoryGateway moodSelectionRepository;
    private final UserStreakRepositoryGateway streakRepository;

    // ==========================================
    // ONBOARDING LẦN ĐẦU
    // ==========================================
    @Override
    public void submitOnboarding(OnboardingRequestDTO dto) {
        String email = currentUserProvider.getCurrentUserEmail();
        Long userId = userServiceGateway.getUserIdByEmail(email)
                .orElseThrow(() -> new BusinessException("User không tồn tại trong auth-service: " + email));

        logger.info("Processing onboarding for user: {}", email);

        // Idempotency + ràng buộc nghiệp vụ: mỗi user chỉ có 1 mục tiêu IN_PROGRESS tại 1 thời điểm,
        // dù mục tiêu mới có trùng tên với mục tiêu đang chạy hay không.
        requireNoActiveGoal(userId);

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
        LocalDate goalStart = today();
        UserGoal primaryGoal = UserGoal.builder()
                .userId(userId)
                .userEmail(email)
                .goalName(currentGoalName)
                .status(GoalStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now(VN_ZONE))
                .startDate(goalStart)
                .endDate(goalStart.plusDays(GOAL_DURATION_DAYS - 1))
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

        // Chặn double-submit + ràng buộc nghiệp vụ: chỉ 1 mục tiêu IN_PROGRESS tại 1 thời điểm
        requireNoActiveGoal(userId);

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
        LocalDate goalStart = today();
        UserGoal newGoal = UserGoal.builder()
                .userId(userId)
                .userEmail(email)
                .goalName(dto.getPrimaryGoal())
                .status(GoalStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now(VN_ZONE))
                .startDate(goalStart)
                .endDate(goalStart.plusDays(GOAL_DURATION_DAYS - 1))
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
    // TRẠNG THÁI HẰNG NGÀY / MỤC TIÊU HIỆN TẠI
    // ==========================================
    @Override
    @Transactional
    public void getDailyTaskStatus(Long goalId) {
        String email = currentUserProvider.getCurrentUserEmail();
        Long userId = userServiceGateway.getUserIdByEmail(email)
                .orElseThrow(() -> new BusinessException("User không tồn tại: " + email));

        requireActiveGoal(userId, goalId);

        LocalDate today = today();
        LocalDate yesterday = today.minusDays(1);

        List<TaskProgressEntity> todayProgress = taskProgressRepository.findByUserIdAndTaskDate(userId, today);
        boolean hasConfirmedToday = todayProgress.size() == REQUIRED_DAILY_TASKS;
        List<TaskResponseDTO> todayTasks = hasConfirmedToday ? buildOrderedTaskDTOs(todayProgress) : List.of();

        List<TaskProgressEntity> yesterdayProgress = taskProgressRepository.findByUserIdAndTaskDate(userId, yesterday);
        boolean hasYesterdayTasks = !yesterdayProgress.isEmpty();
        List<TaskResponseDTO> yesterdayTasks = buildOrderedTaskDTOs(yesterdayProgress);

        outputPort.present(OnboardingResponseDTO.builder()
                .message("Lấy trạng thái nhiệm vụ trong ngày thành công")
                .dailyTaskStatus(DailyTaskStatusResponseDTO.builder()
                        .hasConfirmedToday(hasConfirmedToday)
                        .todayTasks(todayTasks)
                        .hasYesterdayTasks(hasYesterdayTasks)
                        .yesterdayTasks(yesterdayTasks)
                        .build())
                .build());
    }

    @Override
    @Transactional
    public void getCurrentGoal() {
        String email = currentUserProvider.getCurrentUserEmail();
        Long userId = userServiceGateway.getUserIdByEmail(email)
                .orElseThrow(() -> new BusinessException("User không tồn tại: " + email));

        UserGoal goal = goalRepository.findByUserIdAndStatus(userId, GoalStatus.IN_PROGRESS)
                .orElseThrow(() -> new BusinessException("Bạn chưa có mục tiêu nào đang thực hiện"));

        if (goal.isExpired()) {
            goal.setStatus(GoalStatus.COMPLETED);
            goal.setCompletedAt(LocalDateTime.now());
            goalRepository.save(goal);
            throw new BusinessException("Mục tiêu đã hết hạn 4 tuần, vui lòng làm khảo sát lại");
        }

        outputPort.present(OnboardingResponseDTO.builder()
                .message("Lấy mục tiêu hiện tại thành công")
                .currentGoal(CurrentGoalResponseDTO.builder()
                        .goalId(goal.getId())
                        .goalName(goal.getGoalName())
                        .status(goal.getStatus().name())
                        .createdAt(goal.getCreatedAt())
                        .expiresAt(goal.getExpiresAt())
                        .build())
                .build());
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

        LocalDate today = today();

        // Upsert mood selection trong ngày
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

        LocalDate yesterday = today.minusDays(1);
        List<Long> yesterdayTaskIds = taskProgressRepository.findCompletedTaskIdsByUserAndDate(userId, yesterday);

        // Nếu hôm nay đã confirm đủ 5 task rồi -> chỉ trả về đúng 5 task đó (ẩn phần còn lại của pool)
        List<TaskProgressEntity> todayProgress = taskProgressRepository.findByUserIdAndTaskDate(userId, today);
        if (todayProgress.size() == REQUIRED_DAILY_TASKS) {
            List<TaskResponseDTO> confirmedDTOs = buildOrderedTaskDTOs(todayProgress);

            outputPort.present(OnboardingResponseDTO.builder()
                    .message("Bạn đã chọn 5 nhiệm vụ cho hôm nay")
                    .moodTaskResponse(MoodTaskResponseDTO.builder()
                            .moodType(mood.name())
                            .availableTasks(confirmedDTOs)
                            .yesterdayTaskIds(yesterdayTaskIds)
                            .build())
                    .build());
            return;
        }

        // Chưa confirm đủ 5 -> trả full pool theo mood để user chọn
        List<TaskEntity> pool = taskRepository.findByUserIdAndGoalIdAndMoodType(userId, goal.getId(), mood);
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
        if (selectedTaskIds == null
                || selectedTaskIds.size() != REQUIRED_DAILY_TASKS
                || selectedTaskIds.stream().distinct().count() != REQUIRED_DAILY_TASKS) {
            throw new BusinessException("Phải chọn đúng " + REQUIRED_DAILY_TASKS + " nhiệm vụ khác nhau");
        }

        LocalDate today = today();

        // Gán displayOrder theo đúng thứ tự client gửi lên
        for (int i = 0; i < selectedTaskIds.size(); i++) {
            Long taskId = selectedTaskIds.get(i);
            TaskProgressEntity entry = taskProgressRepository.findByUserTaskAndDate(userId, taskId, today)
                    .orElseGet(() -> TaskProgressEntity.builder()
                            .userId(userId)
                            .taskId(taskId)
                            .taskDate(today)
                            .isCompleted(false)
                            .build());
            entry.setDisplayOrder(i);
            taskProgressRepository.save(entry);
        }

        logger.info("User {} đã chọn {} nhiệm vụ cho ngày {}", email, selectedTaskIds.size(), today);

        outputPort.present(OnboardingResponseDTO.builder()
                .message("Đã xác nhận nhiệm vụ hôm nay")
                .build());
    }

    /**
     * Chặn tạo/kích hoạt mục tiêu mới nếu user đang có bất kỳ mục tiêu nào ở trạng thái IN_PROGRESS,
     * bất kể trùng tên hay không. Áp dụng nghiệp vụ: 1 user chỉ có 1 mục tiêu đang chạy tại 1 thời điểm.
     */
    private void requireNoActiveGoal(Long userId) {
        goalRepository.findByUserIdAndStatus(userId, GoalStatus.IN_PROGRESS)
                .ifPresent(g -> {
                    throw new BusinessException("Bạn đang có mục tiêu đang thực hiện, không thể tạo mới");
                });
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

    /** Build DTO theo đúng thứ tự hiển thị của progress, completed lấy từ user_task_daily. */
    private List<TaskResponseDTO> buildOrderedTaskDTOs(List<TaskProgressEntity> progressList) {
        if (progressList.isEmpty()) {
            return List.of();
        }
        List<Long> ids = progressList.stream().map(TaskProgressEntity::getTaskId).toList();
        Map<Long, TaskEntity> taskMap = taskRepository.findByIds(ids).stream()
                .collect(Collectors.toMap(TaskEntity::getId, Function.identity()));
        return toOrderedTaskDTOs(progressList, taskMap);
    }

    private List<TaskResponseDTO> toOrderedTaskDTOs(List<TaskProgressEntity> progressList,
                                                    Map<Long, TaskEntity> taskMap) {
        return progressList.stream()
                .sorted(DAILY_ORDER)
                .map(p -> {
                    TaskEntity entity = taskMap.get(p.getTaskId());
                    if (entity == null) return null;
                    TaskResponseDTO dto = toTaskResponseDTO(entity);
                    dto.setIsCompleted(Boolean.TRUE.equals(p.getIsCompleted()));
                    dto.setProgressId(p.getId());
                    return dto;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Lấy dòng user_task_daily theo progressId và kiểm tra: thuộc user hiện tại, chưa bị xoá mềm, đúng ngày hôm nay.
     * Dùng chung một thông báo lỗi cho mọi trường hợp để không lộ việc id đó có tồn tại hay không.
     */
    private TaskProgressEntity requireTodayProgress(Long userId, Long progressId, LocalDate today) {
        TaskProgressEntity progress = taskProgressRepository.findById(progressId)
                .orElseThrow(() -> new BusinessException("Nhiệm vụ này không thuộc danh sách hôm nay của bạn"));

        if (!userId.equals(progress.getUserId())
                || Boolean.TRUE.equals(progress.getIsDeleted())
                || !today.equals(progress.getTaskDate())) {
            throw new BusinessException("Nhiệm vụ này không thuộc danh sách hôm nay của bạn");
        }
        return progress;
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

        // Kích hoạt lại từ đầu: luôn tính mốc 4 tuần mới kể từ hôm nay, kể cả khi tái sử dụng
        // 1 UserGoal đã tồn tại trước đó (đã COMPLETED) chứ không phải mới tạo lần đầu.
        LocalDate goalStart = today();
        nextGoal.setUserId(userId);
        nextGoal.setStatus(GoalStatus.IN_PROGRESS);
        nextGoal.setStartDate(goalStart);
        nextGoal.setEndDate(goalStart.plusDays(GOAL_DURATION_DAYS - 1));
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
    public void saveTaskProgressItems(Long progressId, TaskProgressRequestDTO requestDTO) {
        String email = currentUserProvider.getCurrentUserEmail();
        Long userId = userServiceGateway.getUserIdByEmail(email)
                .orElseThrow(() -> new BusinessException("User không tồn tại: " + email));

        TaskProgressEntity progress = requireTodayProgress(userId, progressId, today());

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
    public void getTaskProgress(Long progressId) {
        String email = currentUserProvider.getCurrentUserEmail();
        Long userId = userServiceGateway.getUserIdByEmail(email)
                .orElseThrow(() -> new BusinessException("User không tồn tại: " + email));

        LocalDate today = today();
        TaskProgressEntity progress = requireTodayProgress(userId, progressId, today);

        List<String> items = new ArrayList<>();
        if (progress.getSelectedItems() != null && !progress.getSelectedItems().isEmpty()) {
            items = List.of(progress.getSelectedItems().split(","));
        }

        outputPort.present(OnboardingResponseDTO.builder()
                .message("Lấy thông tin tiến độ task thành công")
                .taskProgress(TaskProgressResponseDTO.builder()
                        .progressId(progress.getId())
                        .taskId(progress.getTaskId())
                        .taskDate(today)
                        .isCompleted(Boolean.TRUE.equals(progress.getIsCompleted()))
                        .selectedItems(items)
                        .build())
                .build());
    }

    @Override
    @Transactional
    public void completeTaskProgress(Long progressId) {
        String email = currentUserProvider.getCurrentUserEmail();
        Long userId = userServiceGateway.getUserIdByEmail(email)
                .orElseThrow(() -> new BusinessException("User không tồn tại: " + email));

        LocalDate today = today();

        TaskProgressEntity progress = requireTodayProgress(userId, progressId, today);

        if (Boolean.TRUE.equals(progress.getIsCompleted())) {
            throw new BusinessException("Nhiệm vụ này đã được hoàn thành trước đó");
        }

        progress.setIsCompleted(true);
        taskProgressRepository.save(progress);

        // Kiểm tra nếu đây là task thứ 5/5 hoàn thành trong ngày -> cập nhật streak
        updateStreakIfAllTasksCompleted(userId, today);

        outputPort.present(OnboardingResponseDTO.builder()
                .message("Chúc mừng! Bạn đã hoàn thành nhiệm vụ")
                .taskProgress(TaskProgressResponseDTO.builder()
                        .progressId(progress.getId())
                        .taskId(progress.getTaskId())
                        .taskDate(today)
                        .isCompleted(true)
                        .build())
                .build());
    }

    /**
     * Kiểm tra nếu user đã hoàn thành đủ 5/5 task hôm nay (và đủ 5 task được chọn - không tính task bị xoá mềm).
     * Nếu đủ điều kiện và streak chưa được cập nhật cho ngày hôm nay -> tăng/reset streak.
     */
    private void updateStreakIfAllTasksCompleted(Long userId, LocalDate today) {
        List<TaskProgressEntity> todayProgress = taskProgressRepository.findByUserIdAndTaskDate(userId, today);

        boolean fullSetSelected = todayProgress.size() == REQUIRED_DAILY_TASKS;
        boolean allCompleted = todayProgress.stream().allMatch(TaskProgressEntity::getIsCompleted);

        if (!fullSetSelected || !allCompleted) {
            return; // chưa đủ 5 task hoặc chưa hoàn thành hết
        }

        UserStreakEntity streak = streakRepository.findByUserId(userId)
                .orElseGet(() -> UserStreakEntity.builder()
                        .userId(userId)
                        .currentStreak(0)
                        .longestStreak(0)
                        .build());

        // Tránh cộng streak nhiều lần trong cùng 1 ngày (vd complete task cuối rồi gọi lại API do lỗi mạng)
        if (today.equals(streak.getLastCompletedDate())) {
            return;
        }

        LocalDate yesterday = today.minusDays(1);
        if (yesterday.equals(streak.getLastCompletedDate())) {
            streak.setCurrentStreak(streak.getCurrentStreak() + 1); // nối tiếp chuỗi
        } else {
            streak.setCurrentStreak(1); // đứt chuỗi hoặc lần đầu -> reset về 1
        }

        if (streak.getCurrentStreak() > streak.getLongestStreak()) {
            streak.setLongestStreak(streak.getCurrentStreak());
        }

        streak.setLastCompletedDate(today);
        streakRepository.save(streak);

        logger.info("User {} đạt streak {} ngày (kỷ lục: {})", userId, streak.getCurrentStreak(), streak.getLongestStreak());
    }

    @Override
    @Transactional
    public void swapDailyTask(SwapTaskRequestDTO requestDTO) {
        String email = currentUserProvider.getCurrentUserEmail();
        Long userId = userServiceGateway.getUserIdByEmail(email)
                .orElseThrow(() -> new BusinessException("User không tồn tại: " + email));

        UserGoal goal = requireActiveGoal(userId, requestDTO.getGoalId());

        Long oldTaskId = requestDTO.getOldTaskId();
        Long newTaskId = requestDTO.getNewTaskId();

        if (oldTaskId.equals(newTaskId)) {
            throw new BusinessException("Nhiệm vụ mới phải khác nhiệm vụ cũ");
        }

        LocalDate today = today();

        // 1. Task cũ phải đang nằm trong danh sách hôm nay
        TaskProgressEntity oldProgress = taskProgressRepository.findByUserTaskAndDate(userId, oldTaskId, today)
                .orElseThrow(() -> new BusinessException("Nhiệm vụ cũ không thuộc danh sách hôm nay"));

        // 2. Task đã hoàn thành thì không cho đổi
        if (Boolean.TRUE.equals(oldProgress.getIsCompleted())) {
            throw new BusinessException("Nhiệm vụ đã hoàn thành, không thể thay đổi");
        }

        // 3. Lấy mood đã chọn hôm nay
        DailyMoodSelectionEntity moodSelection = moodSelectionRepository
                .findByUserGoalAndDate(userId, goal.getId(), today)
                .orElseThrow(() -> new BusinessException("Bạn chưa chọn cảm xúc cho hôm nay"));

        // 4. Task mới phải thuộc pool cùng mood
        List<TaskEntity> pool = taskRepository.findByUserIdAndGoalIdAndMoodType(userId, goal.getId(), moodSelection.getMoodType());
        boolean newTaskInPool = pool.stream().anyMatch(t -> t.getId().equals(newTaskId));
        if (!newTaskInPool) {
            throw new BusinessException("Nhiệm vụ mới không thuộc bộ nhiệm vụ theo cảm xúc đã chọn");
        }

        // 5. Task mới không được trùng với các task đang active hôm nay
        List<TaskProgressEntity> todayProgress = taskProgressRepository.findByUserIdAndTaskDate(userId, today);
        boolean alreadySelected = todayProgress.stream().anyMatch(p -> p.getTaskId().equals(newTaskId));
        if (alreadySelected) {
            throw new BusinessException("Nhiệm vụ này đã có trong danh sách hôm nay");
        }

        // 5b. Chuẩn hoá displayOrder (dữ liệu cũ chưa có) và lấy vị trí của task cũ
        List<TaskProgressEntity> orderedToday = todayProgress.stream().sorted(DAILY_ORDER).toList();
        Integer slot = null;
        for (int i = 0; i < orderedToday.size(); i++) {
            TaskProgressEntity p = orderedToday.get(i);
            if (!Integer.valueOf(i).equals(p.getDisplayOrder())) {
                p.setDisplayOrder(i);
                taskProgressRepository.save(p);
            }
            if (p.getTaskId().equals(oldTaskId)) {
                slot = i;
            }
        }

        // 6. Xoá mềm task cũ
        oldProgress.setIsDeleted(true);
        taskProgressRepository.save(oldProgress);

        // 7. Task mới: restore nếu từng tồn tại (kể cả bị xoá mềm), không thì tạo mới; kế thừa vị trí của task cũ
        TaskProgressEntity newProgress = taskProgressRepository.findByUserTaskAndDateIncludingDeleted(userId, newTaskId, today)
                .map(existing -> {
                    existing.setIsDeleted(false);
                    existing.setIsCompleted(false);
                    return existing;
                })
                .orElseGet(() -> TaskProgressEntity.builder()
                        .userId(userId)
                        .taskId(newTaskId)
                        .taskDate(today)
                        .isCompleted(false)
                        .isDeleted(false)
                        .build());
        newProgress.setDisplayOrder(slot);
        taskProgressRepository.save(newProgress);

        // 8. Lấy danh sách 5 task mới nhất sau khi swap (đã sắp đúng thứ tự) để trả về cho client
        List<TaskProgressEntity> updatedProgress = taskProgressRepository.findByUserIdAndTaskDate(userId, today);

        outputPort.present(OnboardingResponseDTO.builder()
                .message("Đã thay đổi nhiệm vụ thành công")
                .tasks(buildOrderedTaskDTOs(updatedProgress)) // Hoặc đưa vào moodTaskResponse tùy theo DTO contract của bạn
                .build());
    }

    @Override
    @Transactional
    public void getCurrentStreak() {
        String email = currentUserProvider.getCurrentUserEmail();
        Long userId = userServiceGateway.getUserIdByEmail(email)
                .orElseThrow(() -> new BusinessException("User không tồn tại: " + email));

        UserStreakEntity streak = streakRepository.findByUserId(userId)
                .orElseGet(() -> UserStreakEntity.builder()
                        .userId(userId)
                        .currentStreak(0)
                        .longestStreak(0)
                        .build());

        // Nếu ngày hoàn thành gần nhất không phải hôm nay và cũng không phải hôm qua -> chuỗi đã đứt, hiển thị 0
        LocalDate today = today();
        LocalDate yesterday = today.minusDays(1);
        int displayStreak = streak.getCurrentStreak();
        if (streak.getLastCompletedDate() != null
                && !streak.getLastCompletedDate().equals(today)
                && !streak.getLastCompletedDate().equals(yesterday)) {
            displayStreak = 0;
        }

        outputPort.present(OnboardingResponseDTO.builder()
                .message("Lấy thông tin chuỗi thành công")
                .streak(StreakResponseDTO.builder()
                        .currentStreak(displayStreak)
                        .longestStreak(streak.getLongestStreak())
                        .lastCompletedDate(streak.getLastCompletedDate())
                        .build())
                .build());
    }

    @Override
    @Transactional
    public void getTaskHistory(LocalDate startDate, LocalDate endDate) {
        String email = currentUserProvider.getCurrentUserEmail();
        Long userId = userServiceGateway.getUserIdByEmail(email)
                .orElseThrow(() -> new BusinessException("User không tồn tại: " + email));

        if (startDate.isAfter(endDate)) {
            throw new BusinessException("startDate phải trước hoặc bằng endDate");
        }

        List<TaskProgressEntity> progressList = taskProgressRepository.findByUserIdAndDateRange(userId, startDate, endDate);

        if (progressList.isEmpty()) {
            outputPort.present(OnboardingResponseDTO.builder()
                    .message("Không có dữ liệu nhiệm vụ trong khoảng thời gian này")
                    .taskHistory(List.of())
                    .build());
            return;
        }

        List<Long> taskIds = progressList.stream().map(TaskProgressEntity::getTaskId).distinct().toList();
        List<TaskEntity> taskEntities = taskRepository.findByIds(taskIds);
        Map<Long, TaskEntity> taskEntityMap = taskEntities.stream()
                .collect(Collectors.toMap(TaskEntity::getId, Function.identity()));

        // Gộp theo taskDate
        Map<LocalDate, List<TaskProgressEntity>> groupedByDate = progressList.stream()
                .collect(Collectors.groupingBy(TaskProgressEntity::getTaskDate));

        List<DailyTaskHistoryDTO> history = groupedByDate.entrySet().stream()
                .sorted(Map.Entry.<LocalDate, List<TaskProgressEntity>>comparingByKey().reversed())
                .map(entry -> DailyTaskHistoryDTO.builder()
                        .taskDate(entry.getKey())
                        .tasks(toOrderedTaskDTOs(entry.getValue(), taskEntityMap))
                        .build())
                .toList();

        outputPort.present(OnboardingResponseDTO.builder()
                .message("Lấy lịch sử nhiệm vụ thành công")
                .taskHistory(history)
                .build());
    }

    private LocalDate today() {
        return LocalDate.now(VN_ZONE);
    }
}