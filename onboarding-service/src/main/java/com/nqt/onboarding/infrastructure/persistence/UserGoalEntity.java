package com.nqt.onboarding.infrastructure.persistence;

import com.nqt.onboarding.domain.GoalStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_goals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGoalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private String goalName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalStatus status;

    private LocalDateTime completedAt;
    private LocalDateTime createdAt;

    // ➕ mới: ngày bắt đầu/kết thúc chu kỳ 4 tuần (giờ VN), gán ngay khi mục tiêu chuyển sang IN_PROGRESS.
    // Nullable để không vỡ các dòng dữ liệu cũ tạo trước khi có 2 cột này (ddl-auto=update sẽ tự thêm cột NULL).
    private LocalDate startDate;
    private LocalDate endDate; // ngày cuối cùng của tuần 4, TÍNH CẢ ngày này (startDate + 27)
}