package com.nqt.checkin.infrastructure.grpc;

import com.nqt.checkin.domain.CheckinRepositoryGateway;
import com.nqt.checkin.infrastructure.persistence.CheckinEntity;
import com.nqt.common.grpc.checkin.CheckinItem;
import com.nqt.common.grpc.checkin.CheckinServiceGrpc;
import com.nqt.common.grpc.checkin.GetCheckinsByDateRangeRequest;
import com.nqt.common.grpc.checkin.GetCheckinsByDateRangeResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * gRPC server nội bộ cho cinema-service. KHÔNG có xác thực, tin tưởng userId truyền vào,
 * nên cổng gRPC chỉ được mở trong mạng Docker, không publish ra ngoài.
 */
@GrpcService
@RequiredArgsConstructor
public class CheckinGrpcService extends CheckinServiceGrpc.CheckinServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(CheckinGrpcService.class);
    private static final long MAX_RANGE_DAYS = 92;

    private final CheckinRepositoryGateway checkinRepositoryGateway;
    private final com.nqt.checkin.infrastructure.storage.SupabaseSignedUrlService supabaseSignedUrlService;

    @Override
    public void getCheckinsByDateRange(GetCheckinsByDateRangeRequest request,
                                       StreamObserver<GetCheckinsByDateRangeResponse> responseObserver) {
        LocalDate start;
        LocalDate end;
        try {
            start = LocalDate.parse(request.getStartDate());
            end = LocalDate.parse(request.getEndDate());
        } catch (DateTimeParseException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Ngày phải có dạng yyyy-MM-dd").asRuntimeException());
            return;
        }

        if (request.getUserId() <= 0
                || end.isBefore(start)
                || ChronoUnit.DAYS.between(start, end) > MAX_RANGE_DAYS) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Tham số không hợp lệ").asRuntimeException());
            return;
        }

        try {
            GetCheckinsByDateRangeResponse.Builder response = GetCheckinsByDateRangeResponse.newBuilder();

            for (CheckinEntity entity : checkinRepositoryGateway.findByUserAndDateRange(request.getUserId(), start, end)) {
                if (entity.getImageUrl() == null || entity.getImageUrl().isBlank()) {
                    continue;
                }
                String signedUrl = supabaseSignedUrlService.createSignedUrl(entity.getImageUrl());
                response.addCheckins(CheckinItem.newBuilder()
                        .setId(entity.getId())
                        .setTaskProgressId(entity.getTaskProgressId())
                        .setCheckinDate(entity.getCheckinDate().toString())
                        .setImageUrl(signedUrl != null ? signedUrl : entity.getImageUrl())
                        .setCreatedAt(entity.getCreatedAt() == null ? "" : entity.getCreatedAt().toString())
                        .build());
            }

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Lỗi khi đọc check-in cho userId {}", request.getUserId(), e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Lỗi khi đọc dữ liệu check-in").asRuntimeException());
        }
    }

    @Override
    public void getPhotoStorageSummary(com.nqt.common.grpc.checkin.GetPhotoStorageSummaryRequest request,
                                       io.grpc.stub.StreamObserver<com.nqt.common.grpc.checkin.GetPhotoStorageSummaryResponse> responseObserver) {
        if (request.getUserId() <= 0) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("userId không hợp lệ").asRuntimeException());
            return;
        }

        try {
            long totalBytes = checkinRepositoryGateway.sumPhotoSizeBytesByUserId(request.getUserId());
            long count = checkinRepositoryGateway.countPhotosByUserId(request.getUserId());

            com.nqt.common.grpc.checkin.GetPhotoStorageSummaryResponse response =
                    com.nqt.common.grpc.checkin.GetPhotoStorageSummaryResponse.newBuilder()
                            .setTotalPhotoSizeBytes(totalBytes)
                            .setPhotoCount(count)
                            .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Lỗi khi đọc photo storage summary cho userId {}", request.getUserId(), e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Lỗi khi đọc dung lượng ảnh check-in").asRuntimeException());
        }
    }
}