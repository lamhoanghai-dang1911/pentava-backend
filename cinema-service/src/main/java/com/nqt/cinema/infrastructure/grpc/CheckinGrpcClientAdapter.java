package com.nqt.cinema.infrastructure.grpc;

import com.nqt.cinema.domain.CheckinPhoto;
import com.nqt.cinema.usecase.boundary.CheckinGateway;
import com.nqt.common.grpc.checkin.CheckinItem;
import com.nqt.common.grpc.checkin.CheckinServiceGrpc;
import com.nqt.common.grpc.checkin.GetCheckinsByDateRangeRequest;
import com.nqt.common.grpc.checkin.GetCheckinsByDateRangeResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class CheckinGrpcClientAdapter implements CheckinGateway {

    private static final Logger logger = LoggerFactory.getLogger(CheckinGrpcClientAdapter.class);
    private static final long TIMEOUT_SECONDS = 10;

    @GrpcClient("checkin-service")
    private CheckinServiceGrpc.CheckinServiceBlockingStub checkinStub;

    @Override
    public List<CheckinPhoto> findByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        try {
            GetCheckinsByDateRangeResponse response = checkinStub
                    .withDeadlineAfter(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .getCheckinsByDateRange(GetCheckinsByDateRangeRequest.newBuilder()
                            .setUserId(userId)
                            .setStartDate(startDate.toString())
                            .setEndDate(endDate.toString())
                            .build());

            return response.getCheckinsList().stream().map(this::toPhoto).toList();

        } catch (Exception e) {
            logger.error("gRPC call getCheckinsByDateRange thất bại: userId={}, {} -> {}", userId, startDate, endDate, e);
            throw new RuntimeException("Không thể lấy ảnh check-in từ checkin-service", e);
        }
    }

    @Override
    public com.nqt.cinema.domain.PhotoStorageSummary getPhotoStorageSummary(Long userId) {
        if (userId == null) return new com.nqt.cinema.domain.PhotoStorageSummary(0L, 0L);
        try {
            com.nqt.common.grpc.checkin.GetPhotoStorageSummaryResponse response = checkinStub
                    .withDeadlineAfter(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .getPhotoStorageSummary(com.nqt.common.grpc.checkin.GetPhotoStorageSummaryRequest.newBuilder()
                            .setUserId(userId)
                            .build());

            return new com.nqt.cinema.domain.PhotoStorageSummary(response.getTotalPhotoSizeBytes(), response.getPhotoCount());
        } catch (Exception e) {
            logger.warn("gRPC call getPhotoStorageSummary thất bại cho userId={}: {}. Sử dụng mặc định 0.", userId, e.getMessage());
            return new com.nqt.cinema.domain.PhotoStorageSummary(0L, 0L);
        }
    }

    private CheckinPhoto toPhoto(CheckinItem item) {
        LocalDateTime createdAt = item.getCreatedAt().isBlank() ? null : LocalDateTime.parse(item.getCreatedAt());
        return new CheckinPhoto(
                item.getId(),
                item.getTaskProgressId(),
                LocalDate.parse(item.getCheckinDate()),
                item.getImageUrl(),
                createdAt
        );
    }
}