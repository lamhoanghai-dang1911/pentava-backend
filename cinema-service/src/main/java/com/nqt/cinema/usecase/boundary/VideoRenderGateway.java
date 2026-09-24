package com.nqt.cinema.usecase.boundary;

import com.nqt.cinema.domain.ClipPhotoRef;

import java.io.File;
import java.util.Comparator;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public interface VideoRenderGateway {

    /** Tải ảnh, ghép thành video MP4 theo spec PENTA-CINEMA. Nhớ gọi cleanup() sau khi dùng xong file. */
    RenderedVideo render(List<ClipPhotoRef> photos) throws Exception;

    record RenderedVideo(File file, Path workDir) {
        public void cleanup() {
            try (var paths = Files.walk(workDir)) {
                paths.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
            } catch (Exception ignored) {
            }
        }
    }
}