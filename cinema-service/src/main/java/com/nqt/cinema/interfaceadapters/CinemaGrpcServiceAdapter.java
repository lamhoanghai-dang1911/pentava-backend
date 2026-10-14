package com.nqt.cinema.interfaceadapters;

import com.nqt.cinema.domain.Clip;
import com.nqt.cinema.domain.ClipStatus;
import com.nqt.cinema.usecase.boundary.ClipRepositoryGateway;
import com.nqt.cinema.usecase.boundary.VideoStorageGateway;
import com.nqt.common.grpc.cinema.CinemaGrpcServiceGrpc;
import com.nqt.common.grpc.cinema.ClipSocialResponse;
import com.nqt.common.grpc.cinema.GetClipForSocialRequest;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.Optional;

@GrpcService
public class CinemaGrpcServiceAdapter extends CinemaGrpcServiceGrpc.CinemaGrpcServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(CinemaGrpcServiceAdapter.class);

    private final ClipRepositoryGateway clipRepositoryGateway;
    private final VideoStorageGateway videoStorageGateway;
    private final long signedUrlExpirySeconds;

    public CinemaGrpcServiceAdapter(
            ClipRepositoryGateway clipRepositoryGateway,
            VideoStorageGateway videoStorageGateway,
            @Value("${cinema.render.social-signed-url-expiry-seconds:315360000}") long signedUrlExpirySeconds) {
        this.clipRepositoryGateway = clipRepositoryGateway;
        this.videoStorageGateway = videoStorageGateway;
        this.signedUrlExpirySeconds = signedUrlExpirySeconds;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public void getClipForSocial(GetClipForSocialRequest request, StreamObserver<ClipSocialResponse> responseObserver) {
        long clipId = request.getClipId();
        long requestingUserId = request.getRequestingUserId();

        log.info("gRPC call getClipForSocial: clipId={}, requestingUserId={}", clipId, requestingUserId);

        try {
            Optional<Clip> clipOpt = clipRepositoryGateway.findById(clipId);
            if (clipOpt.isEmpty()) {
                responseObserver.onNext(ClipSocialResponse.newBuilder()
                        .setFound(false)
                        .setMessage("Clip không tồn tại với ID: " + clipId)
                        .build());
                responseObserver.onCompleted();
                return;
            }

            Clip clip = clipOpt.get();

            String videoUrl = "";
            if (clip.status() == ClipStatus.COMPLETED && clip.storagePath() != null) {
                try {
                    videoUrl = videoStorageGateway.createSignedUrl(clip.storagePath(), signedUrlExpirySeconds);
                } catch (Exception e) {
                    log.error("Không thể tạo signed URL cho clip {}: {}", clipId, e.getMessage());
                }
            }

            ClipSocialResponse response = ClipSocialResponse.newBuilder()
                    .setFound(true)
                    .setClipId(clip.id())
                    .setUserId(clip.userId())
                    .setGoalId(clip.goalId() == null ? 0L : clip.goalId())
                    .setWeekNumber(clip.weekNumber())
                    .setStatus(clip.status().name())
                    .setVideoUrl(videoUrl)
                    .setStoragePath(clip.storagePath() == null ? "" : clip.storagePath())
                    .setPhotoCount(clip.photos() == null ? 0 : clip.photos().size())
                    .setCreatedAt(clip.createdAt() == null ? "" : clip.createdAt().toString())
                    .setMessage("Thành công")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Lỗi khi xử lý gRPC getClipForSocial: clipId={}", clipId, e);
            responseObserver.onNext(ClipSocialResponse.newBuilder()
                    .setFound(false)
                    .setMessage("Lỗi server: " + e.getMessage())
                    .build());
            responseObserver.onCompleted();
        }
    }
}
