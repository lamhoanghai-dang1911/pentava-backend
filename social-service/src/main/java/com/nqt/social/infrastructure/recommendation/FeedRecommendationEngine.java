package com.nqt.social.infrastructure.recommendation;

import com.nqt.social.domain.Post;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class FeedRecommendationEngine {

    private static final double ALPHA_HIGH_FIVE = 2.0;
    private static final double BETA_COMMENT = 4.0;
    private static final double GAMMA_GRAVITY_DECAY = 1.6;
    private static final double COLD_START_BONUS = 5.0;
    private static final double FRIEND_AFFINITY_MULTIPLIER = 1.8;

    public record ScoredPost(Post post, double score) {}

    /**
     * Chấm điểm và xếp hạng bài viết theo thuật toán đề xuất For You
     */
    public List<ScoredPost> scoreAndRank(List<Post> candidatePosts, Long viewerId, Set<Long> friendIds) {
        LocalDateTime now = LocalDateTime.now();

        List<ScoredPost> scoredList = new ArrayList<>();
        for (Post post : candidatePosts) {
            double score = calculateScore(post, now, friendIds);
            scoredList.add(new ScoredPost(post, score));
        }

        // Xếp theo điểm giảm dần, nếu bằng điểm thì bài mới hơn lên trước, sau đó tới ID giảm dần
        scoredList.sort((a, b) -> {
            int cmp = Double.compare(b.score(), a.score());
            if (cmp != 0) return cmp;
            LocalDateTime aTime = a.post().getCreatedAt() != null ? a.post().getCreatedAt() : LocalDateTime.MIN;
            LocalDateTime bTime = b.post().getCreatedAt() != null ? b.post().getCreatedAt() : LocalDateTime.MIN;
            int timeCmp = bTime.compareTo(aTime);
            if (timeCmp != 0) return timeCmp;
            Long aId = a.post().getId() != null ? a.post().getId() : 0L;
            Long bId = b.post().getId() != null ? b.post().getId() : 0L;
            return Long.compare(bId, aId);
        });

        return scoredList;
    }

    private double calculateScore(Post post, LocalDateTime now, Set<Long> friendIds) {
        LocalDateTime postTime = post.getCreatedAt() != null ? post.getCreatedAt() : now;
        double ageInHours = Math.max(0.1, Duration.between(postTime, now).toMinutes() / 60.0);

        long totalInteractions = post.getHighFiveCount() + post.getCommentCount();
        double coldStart = (ageInHours < 24.0 && totalInteractions < 5) ? COLD_START_BONUS : 0.0;

        double baseEngagement = (ALPHA_HIGH_FIVE * post.getHighFiveCount())
                + (BETA_COMMENT * post.getCommentCount())
                + coldStart;

        boolean isFriend = friendIds != null && friendIds.contains(post.getUserId());
        double affinity = isFriend ? FRIEND_AFFINITY_MULTIPLIER : 1.0;

        // Công thức suy giảm theo thời gian (Gravity Decay): (Engagement * Affinity) / (Age + 2)^gamma
        return ((baseEngagement + 1.0) * affinity) / Math.pow(ageInHours + 2.0, GAMMA_GRAVITY_DECAY);
    }

    /**
     * Mã hoá cursor (score:postId) sang chuỗi Base64
     */
    public String encodeCursor(double score, Long postId) {
        String raw = String.format(Locale.US, "%.5f:%d", score, postId);
        return Base64.getUrlEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Giải mã cursor
     */
    public CursorData decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = raw.split(":");
            if (parts.length == 2) {
                return new CursorData(Double.parseDouble(parts[0]), Long.parseLong(parts[1]));
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public record CursorData(double score, long postId) {}
}
