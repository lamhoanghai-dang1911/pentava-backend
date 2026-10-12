package com.nqt.cinema.usecase.interactor;

import com.nqt.cinema.domain.CheckinPhoto;
import com.nqt.cinema.domain.CinemaGoal;
import com.nqt.cinema.infrastructure.security.CurrentUserProvider;
import com.nqt.cinema.usecase.boundary.CheckinGateway;
import com.nqt.cinema.usecase.boundary.CinemaInputPort;
import com.nqt.cinema.usecase.boundary.GoalGateway;
import com.nqt.cinema.usecase.boundary.UserServiceGateway;
import com.nqt.cinema.usecase.dto.GoalSummaryDTO;
import com.nqt.cinema.usecase.dto.PhotoDTO;
import com.nqt.cinema.usecase.dto.WeekPhotosResponseDTO;
import com.nqt.cinema.usecase.dto.WeekSummaryDTO;
import com.nqt.cinema.usecase.dto.WeeksResponseDTO;
import com.nqt.cinema.usecase.exception.CinemaBusinessException;
import com.nqt.cinema.usecase.support.GoalResolver;
import com.nqt.cinema.usecase.support.GoalWeekCalculator;
import com.nqt.cinema.domain.ClipStatus;
import com.nqt.cinema.domain.PhotoStorageSummary;
import com.nqt.cinema.infrastructure.client.ShopSubscriptionClient;
import com.nqt.cinema.usecase.boundary.ClipRepositoryGateway;
import com.nqt.cinema.usecase.dto.CinemaStorageQuotaDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CinemaInteractor implements CinemaInputPort {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final CurrentUserProvider currentUserProvider;
    private final UserServiceGateway userServiceGateway;
    private final CheckinGateway checkinGateway;
    private final GoalGateway goalGateway;
    private final GoalResolver goalResolver;
    private final ClipRepositoryGateway clipRepository;
    private final ShopSubscriptionClient shopSubscriptionClient;
    private final int minPhotos;

    public CinemaInteractor(CurrentUserProvider currentUserProvider,
                            UserServiceGateway userServiceGateway,
                            CheckinGateway checkinGateway,
                            GoalGateway goalGateway,
                            GoalResolver goalResolver,
                            ClipRepositoryGateway clipRepository,
                            ShopSubscriptionClient shopSubscriptionClient,
                            @Value("${cinema.min-photos:3}") int minPhotos) {
        this.currentUserProvider = currentUserProvider;
        this.userServiceGateway = userServiceGateway;
        this.checkinGateway = checkinGateway;
        this.goalGateway = goalGateway;
        this.goalResolver = goalResolver;
        this.clipRepository = clipRepository;
        this.shopSubscriptionClient = shopSubscriptionClient;
        this.minPhotos = minPhotos;
    }

    @Override
    public List<GoalSummaryDTO> getGoals() {
        Long userId = resolveCurrentUserId();
        return goalGateway.findAllByUserId(userId).stream()
                .map(g -> new GoalSummaryDTO(
                        g.id(), g.goalName(), g.status(), g.startDate(), g.endDate(), g.expired()))
                .toList();
    }

    @Override
    public WeeksResponseDTO getGoalWeeks(Long goalId) {
        Long userId = resolveCurrentUserId();
        CinemaGoal goal = goalResolver.resolve(userId, goalId);
        LocalDate today = LocalDate.now(VN_ZONE);

        List<CheckinPhoto> photos =
                checkinGateway.findByUserAndDateRange(userId, goal.startDate(), goal.endDate());
        Map<Integer, Long> photosByWeek = photos.stream()
                .collect(Collectors.groupingBy(
                        p -> GoalWeekCalculator.weekNumberOf(goal.startDate(), p.checkinDate()),
                        Collectors.counting()));

        List<WeekSummaryDTO> weeks = new ArrayList<>();
        for (int n = 1; n <= GoalWeekCalculator.WEEKS_PER_GOAL; n++) {
            LocalDate weekStart = GoalWeekCalculator.weekStartOf(goal.startDate(), n);
            LocalDate weekEnd = GoalWeekCalculator.weekEndOf(goal.startDate(), n);
            int photoCount = photosByWeek.getOrDefault(n, 0L).intValue();
            boolean weekStarted = !today.isBefore(weekStart);
            boolean eligible = weekStarted && photoCount >= minPhotos;
            boolean current = !today.isBefore(weekStart) && !today.isAfter(weekEnd);

            weeks.add(new WeekSummaryDTO(
                    goal.id(), n, weekStart, weekEnd, photoCount, eligible,
                    Math.max(0, minPhotos - photoCount), current));
        }

        return new WeeksResponseDTO(goal.id(), goal.goalName(), minPhotos, weeks);
    }

    @Override
    public WeekPhotosResponseDTO getGoalWeekPhotos(Long goalId, int weekNumber) {
        if (weekNumber < 1 || weekNumber > GoalWeekCalculator.WEEKS_PER_GOAL) {
            throw new CinemaBusinessException("weekNumber phải từ 1 đến " + GoalWeekCalculator.WEEKS_PER_GOAL);
        }

        Long userId = resolveCurrentUserId();
        CinemaGoal goal = goalResolver.resolve(userId, goalId);

        LocalDate weekStart = GoalWeekCalculator.weekStartOf(goal.startDate(), weekNumber);
        LocalDate weekEnd = GoalWeekCalculator.weekEndOf(goal.startDate(), weekNumber);
        LocalDate today = LocalDate.now(VN_ZONE);

        if (today.isBefore(weekStart)) {
            throw new CinemaBusinessException("Tuần " + weekNumber + " chưa bắt đầu");
        }

        List<PhotoDTO> photos = checkinGateway.findByUserAndDateRange(userId, weekStart, weekEnd)
                .stream()
                .map(p -> new PhotoDTO(p.id(), p.taskProgressId(), p.checkinDate(), p.imageUrl(), p.createdAt()))
                .toList();

        return new WeekPhotosResponseDTO(goal.id(), weekNumber, weekStart, weekEnd, photos);
    }

    @Override
    public CinemaStorageQuotaDTO getStorageQuota() {
        Long userId = resolveCurrentUserId();
        PhotoStorageSummary photoSummary = checkinGateway.getPhotoStorageSummary(userId);
        long usedVideosBytes = clipRepository.sumFileSizeBytesByUserId(userId);
        long usedPhotosBytes = photoSummary.totalSizeBytes();
        long totalUsedBytes = usedVideosBytes + usedPhotosBytes;

        ShopSubscriptionClient.UserPlanDetails planDetails = shopSubscriptionClient.getUserSubscriptionDetails(userId);
        long totalBytes = planDetails.storageQuotaBytes();
        long totalClipsCount = clipRepository.countByUserIdAndStatus(userId, ClipStatus.COMPLETED);
        long totalPhotosCount = photoSummary.photoCount();

        double percentage = totalBytes > 0 ? Math.round((totalUsedBytes * 1000.0) / totalBytes) / 10.0 : 0.0;
        if (percentage > 100.0) percentage = 100.0;

        return new CinemaStorageQuotaDTO(
                totalUsedBytes,
                totalBytes,
                usedVideosBytes,
                usedPhotosBytes,
                totalClipsCount,
                totalPhotosCount,
                planDetails.planCode(),
                planDetails.planName(),
                planDetails.hasActiveSubscription(),
                percentage,
                formatBytes(totalUsedBytes),
                formatBytes(totalBytes)
        );
    }

    private String formatBytes(long bytes) {
        if (bytes <= 0) return "0 MB";
        if (bytes < 1024 * 1024) {
            double kb = bytes / 1024.0;
            return String.format("%.1f KB", kb);
        }
        if (bytes < 1024 * 1024 * 1024L) {
            double mb = bytes / (1024.0 * 1024.0);
            return (mb >= 10.0) ? String.format("%.0f MB", mb) : String.format("%.1f MB", mb);
        }
        double gb = bytes / (1024.0 * 1024.0 * 1024.0);
        return String.format("%.2f GB", gb);
    }

    private Long resolveCurrentUserId() {
        String email = currentUserProvider.getCurrentUserEmail();
        return userServiceGateway.getUserIdByEmail(email)
                .orElseThrow(() -> new CinemaBusinessException("User không tồn tại"));
    }
}