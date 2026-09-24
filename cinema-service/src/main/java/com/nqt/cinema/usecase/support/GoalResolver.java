package com.nqt.cinema.usecase.support;

import com.nqt.cinema.domain.CinemaGoal;
import com.nqt.cinema.usecase.boundary.GoalGateway;
import com.nqt.cinema.usecase.exception.CinemaBusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoalResolver {

    private final GoalGateway goalGateway;

    public CinemaGoal resolve(Long userId, Long goalId) {
        CinemaGoal goal = goalGateway.findAllByUserId(userId).stream()
                .filter(g -> g.id().equals(goalId))
                .findFirst()
                .orElseThrow(() -> new CinemaBusinessException("Không tìm thấy mục tiêu"));

        if (goal.startDate() == null || goal.endDate() == null) {
            throw new CinemaBusinessException(
                    "Mục tiêu chưa có ngày bắt đầu/kết thúc, không thể xem theo tuần");
        }
        return goal;
    }
}