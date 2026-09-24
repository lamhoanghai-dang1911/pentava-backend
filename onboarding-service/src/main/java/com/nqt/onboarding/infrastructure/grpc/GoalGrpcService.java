package com.nqt.onboarding.infrastructure.grpc;

import com.nqt.common.grpc.goal.GetUserGoalsRequest;
import com.nqt.common.grpc.goal.GetUserGoalsResponse;
import com.nqt.common.grpc.goal.GoalServiceGrpc;
import com.nqt.common.grpc.goal.UserGoalItem;
import com.nqt.onboarding.domain.UserGoal;
import com.nqt.onboarding.usecase.boundary.UserGoalRepositoryGateway;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * gRPC server nội bộ cho cinema-service. KHÔNG có xác thực, tin tưởng userId truyền vào,
 * nên cổng gRPC chỉ được mở trong mạng Docker, không publish ra ngoài.
 */
@GrpcService
@RequiredArgsConstructor
public class GoalGrpcService extends GoalServiceGrpc.GoalServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(GoalGrpcService.class);

    private final UserGoalRepositoryGateway goalRepository;

    @Override
    public void getUserGoals(GetUserGoalsRequest request,
                             StreamObserver<GetUserGoalsResponse> responseObserver) {
        if (request.getUserId() <= 0) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("userId không hợp lệ").asRuntimeException());
            return;
        }

        try {
            GetUserGoalsResponse.Builder response = GetUserGoalsResponse.newBuilder();

            for (UserGoal goal : goalRepository.findAllByUserId(request.getUserId())) {
                response.addGoals(toGoalItem(goal));
            }

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Lỗi khi đọc danh sách mục tiêu cho userId {}", request.getUserId(), e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Lỗi khi đọc dữ liệu mục tiêu").asRuntimeException());
        }
    }

    private UserGoalItem toGoalItem(UserGoal goal) {
        // weekStart(1)/resolveEndDate() đã tự xử lý goal cũ chưa có startDate/endDate (rơi về createdAt)
        String startDate = goal.weekStart(1) != null ? goal.weekStart(1).toString() : "";
        String endDate = goal.resolveEndDate() != null ? goal.resolveEndDate().toString() : "";

        return UserGoalItem.newBuilder()
                .setId(goal.getId())
                .setGoalName(goal.getGoalName())
                .setStatus(goal.getStatus().name())
                .setStartDate(startDate)
                .setEndDate(endDate)
                .setExpired(goal.isExpired())
                .build();
    }
}