package com.nqt.onboarding.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Builder
public class UserGoal {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    // 4 tuần x 7 ngày = 28 ngày, endDate = startDate + 27 (tính cả 2 đầu)
    private static final int GOAL_DURATION_DAYS = 28;

    private Long id;
    private Long userId;
    private String userEmail;
    private String goalName;
    private GoalStatus status;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;

    // ➕ mới: ngày bắt đầu/kết thúc chu kỳ 4 tuần (giờ VN)
    private LocalDate startDate;
    private LocalDate endDate;

    /**
     * Ngày bắt đầu thật sự dùng để tính toán: ưu tiên startDate, rơi về createdAt (giờ VN)
     * cho các mục tiêu tạo trước khi có 2 cột startDate/endDate.
     */
    private LocalDate resolveStartDate() {
        if (startDate != null) {
            return startDate;
        }
        return createdAt != null ? createdAt.toLocalDate() : null;
    }

    /** Ngày cuối cùng còn tính là "đang chạy" (tính cả ngày này). */
    public LocalDate resolveEndDate() {
        if (endDate != null) {
            return endDate;
        }
        LocalDate start = resolveStartDate();
        return start != null ? start.plusDays(GOAL_DURATION_DAYS - 1) : null;
    }

    /**
     * Khoảng ngày [từ, đến] của tuần thứ weekNumber (1..4) trong chu kỳ mục tiêu.
     * Dùng ở gRPC server trả cho cinema-service; weekNumber ngoài 1..4 ném lỗi để lộ bug sớm.
     */
    public LocalDate weekStart(int weekNumber) {
        requireValidWeekNumber(weekNumber);
        LocalDate start = resolveStartDate();
        return start != null ? start.plusDays(7L * (weekNumber - 1)) : null;
    }

    public LocalDate weekEnd(int weekNumber) {
        LocalDate weekStart = weekStart(weekNumber);
        return weekStart != null ? weekStart.plusDays(6) : null;
    }

    private void requireValidWeekNumber(int weekNumber) {
        if (weekNumber < 1 || weekNumber > 4) {
            throw new IllegalArgumentException("weekNumber phải từ 1 đến 4, nhận: " + weekNumber);
        }
    }

    /**
     * Giữ để tương thích ngược với chỗ đang dùng LocalDateTime (ví dụ CurrentGoalResponseDTO).
     * Trả về thời điểm cuối ngày của resolveEndDate(), tức mục tiêu còn "sống" tới hết ngày đó.
     */
    public LocalDateTime getExpiresAt() {
        LocalDate end = resolveEndDate();
        return end != null ? end.atTime(23, 59, 59) : null;
    }

    public boolean isExpired() {
        LocalDate end = resolveEndDate();
        return status == GoalStatus.IN_PROGRESS
                && end != null
                && LocalDate.now(VN_ZONE).isAfter(end);
    }
}