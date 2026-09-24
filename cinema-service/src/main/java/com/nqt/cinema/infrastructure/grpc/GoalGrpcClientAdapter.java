package com.nqt.cinema.infrastructure.grpc;

import com.nqt.cinema.domain.CinemaGoal;
import com.nqt.cinema.usecase.boundary.GoalGateway;
import com.nqt.common.grpc.goal.GetUserGoalsRequest;
import com.nqt.common.grpc.goal.GetUserGoalsResponse;
import com.nqt.common.grpc.goal.GoalServiceGrpc;
import com.nqt.common.grpc.goal.UserGoalItem;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class GoalGrpcClientAdapter implements GoalGateway {

    private static final Logger logger = LoggerFactory.getLogger(GoalGrpcClientAdapter.class);
    private static final long TIMEOUT_SECONDS = 10;

    @GrpcClient("onboarding-service")
    private GoalServiceGrpc.GoalServiceBlockingStub goalStub;

    @Override
    public List<CinemaGoal> findAllByUserId(Long userId) {
        try {
            GetUserGoalsResponse response = goalStub
                    .withDeadlineAfter(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .getUserGoals(GetUserGoalsRequest.newBuilder()
                            .setUserId(userId)
                            .build());

            return response.getGoalsList().stream().map(this::toGoal).toList();

        } catch (Exception e) {
            logger.error("gRPC call getUserGoals thất bại: userId={}", userId, e);
            throw new RuntimeException("Không thể lấy danh sách mục tiêu từ onboarding-service", e);
        }
    }

    private CinemaGoal toGoal(UserGoalItem item) {
        return new CinemaGoal(
                item.getId(),
                item.getGoalName(),
                item.getStatus(),
                parseDate(item.getStartDate()),
                parseDate(item.getEndDate()),
                item.getExpired()
        );
    }

    // onboarding trả "" khi goal cũ chưa có start_date/end_date
    private static LocalDate parseDate(String value) {
        return (value == null || value.isBlank()) ? null : LocalDate.parse(value);
    }
}