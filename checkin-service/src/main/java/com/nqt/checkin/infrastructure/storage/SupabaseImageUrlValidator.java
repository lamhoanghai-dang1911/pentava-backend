package com.nqt.checkin.infrastructure.storage;

import com.nqt.checkin.usecase.exception.CheckinBusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Chỉ chấp nhận URL ảnh dạng:
 *   https://<project>.supabase.co/storage/v1/object/public/<bucket>/tasks/<...>.(jpg|jpeg|png|webp)
 *
 * Mục đích: cinema-service sẽ tải các URL này về để ghép video, nên không được để client
 * lưu URL tuỳ ý (tránh SSRF trỏ vào địa chỉ nội bộ).
 */
@Component
public class SupabaseImageUrlValidator {

    private static final int MAX_URL_LENGTH = 255; // khớp cột image_url (varchar 255 mặc định)
    private static final Pattern ALLOWED_EXTENSION =
            Pattern.compile(".*\\.(jpe?g|png|webp)$", Pattern.CASE_INSENSITIVE);

    private final String expectedHost;
    private final String bucket;
    private final String pathPrefix;

    public SupabaseImageUrlValidator(@Value("${supabase.url}") String supabaseUrl,
                                     @Value("${supabase.checkin-bucket}") String bucket) {
        String host = null;
        try {
            host = new URI(supabaseUrl.trim()).getHost();
        } catch (URISyntaxException ignored) {
            // xử lý ở dưới
        }
        if (host == null) {
            throw new IllegalStateException("supabase.url không hợp lệ (cần dạng https://<project>.supabase.co)");
        }
        this.expectedHost = host.toLowerCase(Locale.ROOT);
        this.bucket = bucket;
        this.pathPrefix = "/storage/v1/object/public/" + bucket + "/tasks/";
    }

    public void requireValid(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank() || imageUrl.length() > 500) {
            throw invalid();
        }

        String clean = imageUrl.trim();

        // 1. Chấp nhận dạng relative storage path: tasks/{progressId}/{filename}.jpg
        if (clean.startsWith("tasks/") && !clean.contains("..")) {
            String pathWithoutQuery = clean.split("\\?")[0];
            if (ALLOWED_EXTENSION.matcher(pathWithoutQuery).matches()) {
                return;
            }
        }

        // 2. Chấp nhận full URL từ Supabase dự án (public, sign, authenticated hoặc direct object)
        URI uri;
        try {
            uri = new URI(clean);
        } catch (URISyntaxException e) {
            throw invalid();
        }

        String path = uri.getPath(); // đã decode, nên %2e%2e cũng bị bắt bởi kiểm tra ".."
        boolean isSupabaseStoragePath = path != null
                && (path.startsWith(pathPrefix)
                || path.startsWith("/storage/v1/object/sign/" + bucket + "/tasks/")
                || path.startsWith("/storage/v1/object/authenticated/" + bucket + "/tasks/")
                || path.startsWith("/storage/v1/object/" + bucket + "/tasks/"));

        boolean valid = "https".equalsIgnoreCase(uri.getScheme())
                && uri.getUserInfo() == null
                && uri.getPort() == -1
                && uri.getFragment() == null
                && uri.getHost() != null
                && expectedHost.equalsIgnoreCase(uri.getHost())
                && isSupabaseStoragePath
                && !path.contains("..")
                && ALLOWED_EXTENSION.matcher(path).matches();

        if (!valid) {
            throw invalid();
        }
    }

    private CheckinBusinessException invalid() {
        return new CheckinBusinessException("Đường dẫn ảnh không hợp lệ");
    }
}