package com.nqt.onboarding.usecase.boundary;

import com.nqt.onboarding.domain.UserGoal;
import com.nqt.onboarding.domain.GoalStatus;

import java.util.List;
import java.util.Optional;

public interface UserGoalRepositoryGateway {
    Optional<UserGoal> findByEmailAndGoalName(String email, String goalName);
    Optional<UserGoal> findById(Long id);
    Optional<UserGoal> findByUserIdAndStatus(Long userId, GoalStatus status);

    // ➕ mới: toàn bộ mục tiêu của 1 user (mọi trạng thái), mới nhất trước — dùng cho cinema-service
    // liệt kê danh sách mục tiêu để xem lại clip cũ, kể cả mục tiêu đã COMPLETED hoặc lặp lại tên cũ.
    List<UserGoal> findAllByUserId(Long userId);

    UserGoal save(UserGoal goal);
}