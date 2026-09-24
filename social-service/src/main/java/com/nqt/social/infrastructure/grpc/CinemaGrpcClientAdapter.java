package com.nqt.social.infrastructure.grpc;

import com.nqt.common.grpc.cinema.CinemaGrpcServiceGrpc;
import com.nqt.common.grpc.cinema.ClipSocialResponse;
import com.nqt.common.grpc.cinema.GetClipForSocialRequest;
import com.nqt.social.usecase.boundary.CinemaServiceGateway;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class CinemaGrpcClientAdapter implements CinemaServiceGateway {

    private static final Logger log = LoggerFactory.getLogger(CinemaGrpcClientAdapter.class);
    private static final long TIMEOUT_SECONDS = 5;

    @GrpcClient("cinema-service")
    private CinemaGrpcServiceGrpc.CinemaGrpcServiceBlockingStub cinemaServiceStub;

    @Override
    public Optional<ClipSocialInfo> getClipForSocial(Long clipId, Long requestingUserId) {
        try {
            ClipSocialResponse response = cinemaServiceStub
                    .withDeadlineAfter(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .getClipForSocial(GetClipForSocialRequest.newBuilder()
                            .setClipId(clipId)
                            .setRequestingUserId(requestingUserId)
                            .build());

            if (!response.getFound()) {
                log.warn("Clip không tìm thấy tại cinema-service với ID: {}. Lý do: {}", clipId, response.getMessage());
                return Optional.empty();
            }

            return Optional.of(new ClipSocialInfo(
                    response.getClipId(),
                    response.getUserId(),
                    response.getGoalId(),
                    response.getWeekNumber(),
                    response.getStatus(),
                    response.getVideoUrl(),
                    response.getStoragePath(),
                    response.getPhotoCount()
            ));

        } catch (Exception e) {
            log.error("gRPC call getClipForSocial thất bại với clipId: {}", clipId, e);
            return Optional.empty();
        }
    }
}
