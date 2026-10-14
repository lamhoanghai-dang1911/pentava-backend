package com.nqt.cinema.usecase.interactor;

import com.nqt.cinema.domain.CheckinPhoto;
import com.nqt.cinema.domain.CinemaGoal;
import com.nqt.cinema.domain.Clip;
import com.nqt.cinema.domain.ClipPhotoRef;
import com.nqt.cinema.domain.ClipStatus;
import com.nqt.cinema.infrastructure.security.CurrentUserProvider;
import com.nqt.cinema.usecase.boundary.CheckinGateway;
import com.nqt.cinema.usecase.boundary.ClipInputPort;
import com.nqt.cinema.usecase.boundary.ClipRepositoryGateway;
import com.nqt.cinema.usecase.boundary.UserServiceGateway;
import com.nqt.cinema.usecase.boundary.VideoStorageGateway;
import com.nqt.cinema.usecase.dto.ClipDTO;
import com.nqt.cinema.usecase.dto.CreateClipRequestDTO;
import com.nqt.cinema.usecase.exception.CinemaBusinessException;
import com.nqt.cinema.usecase.support.GoalResolver;
import com.nqt.cinema.usecase.support.GoalWeekCalculator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Service
public class ClipInteractor implements ClipInputPort {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final int MAX_CLIPS_PER_WEEK = 3;

    private final CurrentUserProvider currentUserProvider;
    private final UserServiceGateway userServiceGateway;
    private final CheckinGateway checkinGateway;
    private final GoalResolver goalResolver;
    private final ClipRepositoryGateway clipRepository;
    private final ClipRenderWorker clipRenderWorker;
    private final VideoStorageGateway videoStorageGateway;
    private final com.nqt.cinema.infrastructure.client.ShopSubscriptionClient shopSubscriptionClient;
    private final long signedUrlExpirySeconds;

    public ClipInteractor(CurrentUserProvider currentUserProvider,
                          UserServiceGateway userServiceGateway,
                          CheckinGateway checkinGateway,
                          GoalResolver goalResolver,
                          ClipRepositoryGateway clipRepository,
                          ClipRenderWorker clipRenderWorker,
                          VideoStorageGateway videoStorageGateway,
                          com.nqt.cinema.infrastructure.client.ShopSubscriptionClient shopSubscriptionClient,
                          @Value("${cinema.render.signed-url-expiry-seconds:21600}") long signedUrlExpirySeconds) {
        this.currentUserProvider = currentUserProvider;
        this.userServiceGateway = userServiceGateway;
        this.checkinGateway = checkinGateway;
        this.goalResolver = goalResolver;
        this.clipRepository = clipRepository;
        this.clipRenderWorker = clipRenderWorker;
        this.videoStorageGateway = videoStorageGateway;
        this.shopSubscriptionClient = shopSubscriptionClient;
        this.signedUrlExpirySeconds = signedUrlExpirySeconds;
    }

    @Override
    @Transactional
    public ClipDTO createClip(Long goalId, int weekNumber, CreateClipRequestDTO request) {
        Long userId = resolveCurrentUserId();
        CinemaGoal goal = goalResolver.resolve(userId, goalId);

        // 1. Kiểm tra hạn mức dung lượng lưu trữ (STORAGE_EXPANSION)
        checkStorageQuota(userId);

        if (weekNumber < 1 || weekNumber > GoalWeekCalculator.WEEKS_PER_GOAL) {
            throw new CinemaBusinessException("weekNumber phải từ 1 đến " + GoalWeekCalculator.WEEKS_PER_GOAL);
        }

        LocalDate weekStart = GoalWeekCalculator.weekStartOf(goal.startDate(), weekNumber);
        LocalDate weekEnd = GoalWeekCalculator.weekEndOf(goal.startDate(), weekNumber);
        LocalDate today = LocalDate.now(VN_ZONE);
        if (today.isBefore(weekStart)) {
            throw new CinemaBusinessException("Tuần " + weekNumber + " chưa bắt đầu");
        }

        if (clipRepository.hasActiveJob(userId)) {
            throw new CinemaBusinessException("Bạn đang có 1 video khác đang xử lý, vui lòng đợi xong rồi tạo tiếp");
        }

        long completedThisWeek = clipRepository.countByUserGoalWeekAndStatus(
                userId, goalId, weekNumber, ClipStatus.COMPLETED);
        if (completedThisWeek >= MAX_CLIPS_PER_WEEK) {
            throw new CinemaBusinessException("Đã đạt giới hạn " + MAX_CLIPS_PER_WEEK + " video cho tuần này");
        }

        List<Long> requestedIds = request.photoIds();
        if (new LinkedHashSet<>(requestedIds).size() != requestedIds.size()) {
            throw new CinemaBusinessException("Danh sách ảnh bị trùng lặp");
        }

        Map<Long, CheckinPhoto> byId = new HashMap<>();
        for (CheckinPhoto p : checkinGateway.findByUserAndDateRange(userId, weekStart, weekEnd)) {
            byId.put(p.id(), p);
        }

        List<ClipPhotoRef> photoRefs = new ArrayList<>();
        int order = 0;
        for (Long photoId : requestedIds) {
            CheckinPhoto photo = byId.get(photoId);
            if (photo == null) {
                throw new CinemaBusinessException("Ảnh id=" + photoId + " không hợp lệ hoặc không thuộc tuần này");
            }
            photoRefs.add(new ClipPhotoRef(photo.id(), photo.imageUrl(), photo.checkinDate(), order++));
        }

        Clip saved = clipRepository.create(
                new Clip(null, userId, goalId, weekNumber, ClipStatus.PENDING, null, null, null, null, null, photoRefs));

        clipRenderWorker.renderAsync(saved.id());

        return toDto(saved, null);
    }

    @Override
    @Transactional
    public ClipDTO createJourneyClip(Long goalId, com.nqt.cinema.usecase.dto.CreateJourneyClipRequestDTO request) {
        Long userId = resolveCurrentUserId();

        // 1. Kiểm tra quyền sở hữu tính năng Multi-Angle & Dynamic Templates (Ghép hành trình nhiều tuần)
        if (!shopSubscriptionClient.hasMultiAngleTemplates(userId)) {
            throw new CinemaBusinessException("Tính năng tạo video ghép hành trình nhiều tuần yêu cầu gói cước nâng cao (Gold hoặc Premium). Vui lòng nâng cấp gói để sử dụng!");
        }

        // 2. Kiểm tra hạn mức dung lượng lưu trữ (STORAGE_EXPANSION)
        checkStorageQuota(userId);

        if (clipRepository.hasActiveJob(userId)) {
            throw new CinemaBusinessException("Bạn đang có 1 video khác đang xử lý, vui lòng đợi xong rồi tạo tiếp");
        }

        CinemaGoal goal = goalResolver.resolve(userId, goalId);
        List<Long> requestedIds = request.photoIds();
        if (new LinkedHashSet<>(requestedIds).size() != requestedIds.size()) {
            throw new CinemaBusinessException("Danh sách ảnh bị trùng lặp");
        }

        // Lấy ảnh qua toàn bộ 4 tuần của mục tiêu
        LocalDate fullGoalStart = GoalWeekCalculator.weekStartOf(goal.startDate(), 1);
        LocalDate fullGoalEnd = GoalWeekCalculator.weekEndOf(goal.startDate(), GoalWeekCalculator.WEEKS_PER_GOAL);

        Map<Long, CheckinPhoto> byId = new HashMap<>();
        for (CheckinPhoto p : checkinGateway.findByUserAndDateRange(userId, fullGoalStart, fullGoalEnd)) {
            byId.put(p.id(), p);
        }

        List<ClipPhotoRef> photoRefs = new ArrayList<>();
        int order = 0;
        for (Long photoId : requestedIds) {
            CheckinPhoto photo = byId.get(photoId);
            if (photo == null) {
                throw new CinemaBusinessException("Ảnh id=" + photoId + " không hợp lệ hoặc không thuộc mục tiêu này");
            }
            photoRefs.add(new ClipPhotoRef(photo.id(), photo.imageUrl(), photo.checkinDate(), order++));
        }

        // weekNumber = 0 biểu thị clip ghép hành trình tổng hợp nhiều tuần
        Clip saved = clipRepository.create(
                new Clip(null, userId, goalId, 0, ClipStatus.PENDING, null, null, null, null, null, photoRefs));

        clipRenderWorker.renderAsync(saved.id());

        return toDto(saved, null);
    }

    private void checkStorageQuota(Long userId) {
        long quotaBytes = shopSubscriptionClient.getStorageQuotaBytes(userId);
        long videoUsage = clipRepository.sumFileSizeBytesByUserId(userId);
        long photoUsage = checkinGateway.getPhotoStorageSummary(userId).totalSizeBytes();
        long totalUsage = videoUsage + photoUsage;
        if (totalUsage >= quotaBytes) {
            throw new CinemaBusinessException(String.format(
                    "Dung lượng lưu trữ của bạn đã đầy (đã dùng %,d MB / %,d MB bao gồm cả clip và hình ảnh). Vui lòng nâng cấp gói cước để lưu thêm video hoặc xóa bớt video cũ.",
                    totalUsage / (1024 * 1024), quotaBytes / (1024 * 1024)
            ));
        }
    }

    @Override
    public List<ClipDTO> getClips(Long goalId, int weekNumber) {
        Long userId = resolveCurrentUserId();
        return clipRepository.findByUserGoalWeek(userId, goalId, weekNumber).stream()
                .map(c -> toDto(c, signedUrlOrNull(c)))
                .toList();
    }

    @Override
    public ClipDTO getClip(Long clipId) {
        Long userId = resolveCurrentUserId();
        Clip clip = clipRepository.findByIdAndUserId(clipId, userId)
                .orElseThrow(() -> new CinemaBusinessException("Không tìm thấy clip"));
        return toDto(clip, signedUrlOrNull(clip));
    }

    @Override
    public void deleteClip(Long clipId) {
        Long userId = resolveCurrentUserId();
        Clip clip = clipRepository.findByIdAndUserId(clipId, userId)
                .orElseThrow(() -> new CinemaBusinessException("Không tìm thấy clip"));

        if (clip.status() == ClipStatus.PROCESSING || clip.status() == ClipStatus.UPLOADING) {
            throw new CinemaBusinessException("Clip đang xử lý, không thể xoá ngay lúc này");
        }

        if (clip.storagePath() != null) {
            try {
                videoStorageGateway.delete(clip.storagePath());
            } catch (Exception e) {
                throw new RuntimeException("Không thể xoá file video trên Supabase", e);
            }
        }

        clipRepository.delete(clipId);
    }

    private String signedUrlOrNull(Clip clip) {
        if (clip.status() != ClipStatus.COMPLETED || clip.storagePath() == null) {
            return null;
        }
        try {
            return videoStorageGateway.createSignedUrl(clip.storagePath(), signedUrlExpirySeconds);
        } catch (Exception e) {
            throw new RuntimeException("Không tạo được đường dẫn xem video", e);
        }
    }

    private ClipDTO toDto(Clip clip, String videoUrl) {
        String errorMsg = clip.errorMessage();
        if (errorMsg != null && (errorMsg.toLowerCase().contains("ffmpeg")
                || errorMsg.toLowerCase().contains("cannot run program")
                || errorMsg.contains("error=2")
                || errorMsg.contains("No such file"))) {
            errorMsg = "Quá trình tạo video trước đây bị gián đoạn. Bạn có thể xóa để tạo lại.";
        }
        return new ClipDTO(clip.id(), clip.goalId(), clip.weekNumber(), clip.status().name(),
                videoUrl, errorMsg, clip.photos().size(), clip.createdAt());
    }

    private Long resolveCurrentUserId() {
        String email = currentUserProvider.getCurrentUserEmail();
        return userServiceGateway.getUserIdByEmail(email)
                .orElseThrow(() -> new CinemaBusinessException("User không tồn tại"));
    }
}