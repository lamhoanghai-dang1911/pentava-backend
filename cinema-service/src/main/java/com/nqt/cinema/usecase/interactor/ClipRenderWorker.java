package com.nqt.cinema.usecase.interactor;

import com.nqt.cinema.domain.Clip;
import com.nqt.cinema.domain.ClipStatus;
import com.nqt.cinema.usecase.boundary.ClipRepositoryGateway;
import com.nqt.cinema.usecase.boundary.VideoRenderGateway;
import com.nqt.cinema.usecase.boundary.VideoStorageGateway;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClipRenderWorker {

    private static final Logger logger = LoggerFactory.getLogger(ClipRenderWorker.class);

    private final ClipRepositoryGateway clipRepository;
    private final VideoRenderGateway videoRenderGateway;
    private final VideoStorageGateway videoStorageGateway;

    @Async("clipRenderExecutor")
    @Transactional
    public void renderAsync(Long clipId) {

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Clip clip = clipRepository.findById(clipId).orElse(null);
        if (clip == null) {
            logger.warn("Clip {} không tồn tại khi bắt đầu render", clipId);
            return;
        }

        VideoRenderGateway.RenderedVideo rendered = null;
        try {
            clipRepository.updateStatus(clipId, ClipStatus.PROCESSING, null, null);
            rendered = videoRenderGateway.render(clip.photos());

            clipRepository.updateStatus(clipId, ClipStatus.UPLOADING, null, null);
            String storagePath = videoStorageGateway.upload(rendered.file(), clip.userId(), clipId);

            clipRepository.updateStatus(clipId, ClipStatus.COMPLETED, storagePath, null);
        } catch (Exception e) {
            logger.error("Render clip {} thất bại", clipId, e);
            clipRepository.updateStatus(clipId, ClipStatus.FAILED, null, safeMessage(e));
        } finally {
            if (rendered != null) {
                rendered.cleanup();
            }
        }
    }

    private static String safeMessage(Exception e) {
        String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        return msg.length() > 900 ? msg.substring(0, 900) : msg;
    }
}