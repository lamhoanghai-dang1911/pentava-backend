package com.nqt.cinema.infrastructure.render;

import com.nqt.cinema.domain.ClipPhotoRef;
import com.nqt.cinema.usecase.boundary.VideoRenderGateway;
import com.nqt.cinema.usecase.exception.CinemaBusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Component
public class FfmpegVideoRenderAdapter implements VideoRenderGateway {

    private static final Logger logger = LoggerFactory.getLogger(FfmpegVideoRenderAdapter.class);

    private static final int WIDTH = 1080;
    private static final int HEIGHT = 1920;
    private static final int FPS = 30;
    private static final double IMAGE_DISPLAY_SECONDS = 2.0;
    private static final double CROSSFADE_SECONDS = 0.4;
    private static final long DOWNLOAD_TIMEOUT_SECONDS = 20;
    private static final long MAX_IMAGE_BYTES = 15L * 1024 * 1024;
    private static final long FFMPEG_TIMEOUT_SECONDS = 180;

    private final String fontFilePath;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(DOWNLOAD_TIMEOUT_SECONDS))
            .build();

    public FfmpegVideoRenderAdapter(
            @Value("${cinema.render.font-file:/usr/share/fonts/truetype/noto/NotoSans-Regular.ttf}") String fontFilePath) {
        this.fontFilePath = fontFilePath;
    }

    @Override
    public RenderedVideo render(List<ClipPhotoRef> photos) throws Exception {
        if (photos.size() < 2) {
            throw new CinemaBusinessException("Cần ít nhất 2 ảnh để tạo video");
        }

        Path workDir = Files.createTempDirectory("cinema-render-");
        List<File> downloaded = downloadImages(photos, workDir);

        File output = workDir.resolve("output.mp4").toFile();
        List<String> command = buildFfmpegCommand(downloaded, photos, output);

        logger.info("Chạy ffmpeg cho {} ảnh", photos.size());

        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();

        String logOutput;
        try (var reader = process.inputReader()) {
            logOutput = reader.lines().reduce("", (a, b) -> a + "\n" + b);
        }

        boolean finished = process.waitFor(FFMPEG_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("ffmpeg chạy quá " + FFMPEG_TIMEOUT_SECONDS + "s, đã huỷ");
        }
        if (process.exitValue() != 0) {
            logger.error("ffmpeg thất bại (exit {}):\n{}", process.exitValue(), logOutput);
            throw new RuntimeException("ffmpeg render thất bại, xem log server để biết chi tiết");
        }
        if (!output.exists() || output.length() == 0) {
            throw new RuntimeException("ffmpeg chạy xong nhưng không sinh ra file video");
        }

        return new RenderedVideo(output, workDir);
    }

    private List<File> downloadImages(List<ClipPhotoRef> photos, Path workDir) throws Exception {
        List<File> files = new ArrayList<>();
        for (int i = 0; i < photos.size(); i++) {
            String url = photos.get(i).imageUrl();
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(DOWNLOAD_TIMEOUT_SECONDS))
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Không tải được ảnh (HTTP " + response.statusCode() + "): " + url);
            }
            byte[] body = response.body();
            if (body.length == 0 || body.length > MAX_IMAGE_BYTES) {
                throw new RuntimeException("Ảnh không hợp lệ hoặc quá lớn: " + url);
            }

            File imgFile = workDir.resolve("img_" + i + ".jpg").toFile();
            Files.write(imgFile.toPath(), body);
            files.add(imgFile);
        }
        return files;
    }

    private List<String> buildFfmpegCommand(List<File> images, List<ClipPhotoRef> photos, File output) {
        List<String> cmd = new ArrayList<>();
        cmd.add("ffmpeg");
        cmd.add("-y");

        for (File img : images) {
            cmd.add("-loop"); cmd.add("1");
            cmd.add("-t"); cmd.add(fmt(IMAGE_DISPLAY_SECONDS));
            cmd.add("-i"); cmd.add(img.getAbsolutePath());
        }

        cmd.add("-filter_complex");
        cmd.add(buildFilterComplex(images.size(), photos));

        cmd.add("-map"); cmd.add("[vout]");
        cmd.add("-r"); cmd.add(String.valueOf(FPS));
        cmd.add("-pix_fmt"); cmd.add("yuv420p");
        cmd.add("-c:v"); cmd.add("libx264");
        cmd.add("-movflags"); cmd.add("+faststart");
        cmd.add("-an");
        cmd.add(output.getAbsolutePath());

        return cmd;
    }

    private String buildFilterComplex(int n, List<ClipPhotoRef> photos) {
        StringBuilder sb = new StringBuilder();

        // Mỗi ảnh: nền mờ phủ kín khung (không crop ảnh gốc) + ảnh gốc canh giữa + nhãn ngày
        for (int i = 0; i < n; i++) {
            String label = escapeDrawtext(dateLabel(photos.get(i).checkinDate()));

            sb.append("[").append(i).append(":v]split=2[base").append(i).append("a][base").append(i).append("b];");
            sb.append("[base").append(i).append("a]scale=").append(WIDTH).append(":").append(HEIGHT)
                    .append(":force_original_aspect_ratio=increase,crop=").append(WIDTH).append(":").append(HEIGHT)
                    .append(",gblur=sigma=20,setsar=1[bg").append(i).append("];");
            sb.append("[base").append(i).append("b]scale=").append(WIDTH).append(":").append(HEIGHT)
                    .append(":force_original_aspect_ratio=decrease,setsar=1[fg").append(i).append("];");
            sb.append("[bg").append(i).append("][fg").append(i).append("]overlay=(W-w)/2:(H-h)/2[comp").append(i).append("];");
            sb.append("[comp").append(i).append("]drawtext=fontfile='").append(fontFilePath).append("'")
                    .append(":text='").append(label).append("'")
                    .append(":x=40:y=60:fontsize=44:fontcolor=white")
                    .append(":box=1:boxcolor=black@0.45:boxborderw=14")
                    .append(",format=yuv420p,fps=").append(FPS).append("[v").append(i).append("];");
        }

        // Nối các đoạn bằng xfade — offset = i × (thời_lượng_ảnh - thời_lượng_chuyển_cảnh)
        String prevLabel = "v0";
        double offset = IMAGE_DISPLAY_SECONDS - CROSSFADE_SECONDS;
        for (int i = 1; i < n; i++) {
            String outLabel = (i == n - 1) ? "vout" : "x" + i;
            sb.append("[").append(prevLabel).append("][v").append(i).append("]xfade=transition=fade:duration=")
                    .append(fmt(CROSSFADE_SECONDS)).append(":offset=").append(fmt(offset))
                    .append("[").append(outLabel).append("];");
            prevLabel = outLabel;
            offset += (IMAGE_DISPLAY_SECONDS - CROSSFADE_SECONDS);
        }

        if (!sb.isEmpty() && sb.charAt(sb.length() - 1) == ';') {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    private static String fmt(double value) {
        return String.format(Locale.ROOT, "%.3f", value);
    }

    private static String dateLabel(LocalDate date) {
        if (date == null) {
            return "";
        }
        String weekday = switch (date.getDayOfWeek()) {
            case MONDAY -> "Thứ Hai";
            case TUESDAY -> "Thứ Ba";
            case WEDNESDAY -> "Thứ Tư";
            case THURSDAY -> "Thứ Năm";
            case FRIDAY -> "Thứ Sáu";
            case SATURDAY -> "Thứ Bảy";
            case SUNDAY -> "Chủ Nhật";
        };
        return weekday + " " + String.format(Locale.ROOT, "%02d/%02d", date.getDayOfMonth(), date.getMonthValue());
    }

    // Chỉ escape ':' và '\' theo cú pháp filter ffmpeg — nhãn ngày không có dấu nháy đơn nên
    // chưa xử lý escape dấu nháy đơn ở đây.
    private static String escapeDrawtext(String text) {
        return text.replace("\\", "\\\\").replace(":", "\\:");
    }
}