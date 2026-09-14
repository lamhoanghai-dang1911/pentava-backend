package com.nqt.onboarding.domain;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class UserGoal {
    private Long id;
    private Long userId;
    private String userEmail;
    private String goalName;
    private GoalStatus status;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;

    public LocalDateTime getExpiresAt() {
        return createdAt != null ? createdAt.plusWeeks(4) : null;
    }

    public boolean isExpired() {
        return status == GoalStatus.IN_PROGRESS
                && getExpiresAt() != null
                && LocalDateTime.now().isAfter(getExpiresAt());
    }
}