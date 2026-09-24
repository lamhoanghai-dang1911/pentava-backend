package com.nqt.cinema.usecase.boundary;

import com.nqt.cinema.domain.CinemaGoal;

import java.util.List;

public interface GoalGateway {
    /** Toàn bộ mục tiêu của user, mọi trạng thái, lấy từ onboarding-service qua gRPC. */
    List<CinemaGoal> findAllByUserId(Long userId);
}